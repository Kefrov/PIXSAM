package com.example.pixsam;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pixsam.pixsam_db.DrawingItem;
import com.example.pixsam.pixsam_db.PixsamDatabase;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import android.util.Log;

public class RecyclerViewAdapter extends  RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private final List<DrawingItem> drawingList;
    List<DrawingItem> fullList;
    private final Context context;
    public RecyclerViewAdapter(Context context, List<DrawingItem> drawingList) {
        this.context = context;
        this.drawingList = drawingList;
        this.fullList = new ArrayList<>(drawingList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.saveddrawing_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DrawingItem curr_drawing = drawingList.get(position);
        holder.drawing_name.setText(curr_drawing.getDrawing_name());

        holder.itemView.setOnClickListener(v -> {
            // Get the drawing ID and pass it to MainActivity through Intent and draw the drawing that are one the database
            Intent intent = new Intent(context, MainUIActivity.class);
            Log.d("item_view listener",String.valueOf(curr_drawing.getDrawing_id()));
            intent.putExtra("drawing_id", curr_drawing.getDrawing_id());
            intent.putExtra("source", "recycler_view_item");
            context.startActivity(intent);
        });
        holder.itemView.setOnLongClickListener(v -> {
            showDeleteRename(holder,curr_drawing);
            return true;
        });
    }

    private void showDeleteRename(ViewHolder holder,DrawingItem curr_drawing) {
        int pos = holder.getAdapterPosition();
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        @SuppressLint("InflateParams") View dialogView = LayoutInflater.from(context).inflate(R.layout.delete_rename, null);
        bottomSheetDialog.setContentView(dialogView);

        ImageButton btn_delete = dialogView.findViewById(R.id.btn_delete);
        ImageButton btn_rename= dialogView.findViewById(R.id.btn_rename);

        btn_delete.setOnClickListener(v1 -> new AlertDialog.Builder(v1.getContext())
                .setTitle("Delete Drawing")
                .setMessage("Are you sure you want to delete this drawing?")
                .setPositiveButton("Yes", (dialog, which) -> new Thread (() -> {
                    PixsamDatabase.getDatabase(context).pixsamDao().deleteDrawing(curr_drawing);
                    ((Activity) context).runOnUiThread (() -> {
                        DrawingItem drawingToDelete = drawingList.get(pos);
                        drawingList.remove(pos);
                        fullList.remove(drawingToDelete);
                        notifyItemRemoved(pos);
                        bottomSheetDialog.dismiss();
                    });
                }).start())
                .setNegativeButton("No", null)
                .show());
        btn_rename.setOnClickListener(v1 -> {
            @SuppressLint("InflateParams") View dgView = LayoutInflater.from(context).inflate(R.layout.rename, null);
            Dialog dialog = new Dialog(context);
            dialog.setContentView(dgView);
            // rounded bg
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            // Set width and height
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);

            EditText editText = dgView.findViewById(R.id.rename_txt);
            editText.setText(curr_drawing.getDrawing_name());

            Button btnOk = dgView.findViewById(R.id.btn_ok);
            Button btnCancel = dgView.findViewById(R.id.btn_cancel);

            btnOk.setOnClickListener(v -> {
                String newName = editText.getText().toString().trim();
                if (!newName.isEmpty()) {
                    curr_drawing.setDrawing_name(newName);
                    new Thread(() -> {
                        PixsamDatabase.getDatabase(context).pixsamDao().updateDrawing(curr_drawing);
                        ((Activity) context).runOnUiThread(() -> {
                            drawingList.get(pos).setDrawing_name(newName);
                            fullList.get(pos).setDrawing_name(newName);
                            notifyItemChanged(pos);
                            dialog.dismiss();
                            bottomSheetDialog.dismiss();
                        });
                    }).start();
                }
            });
            btnCancel.setOnClickListener(v -> {
                dialog.dismiss();
                bottomSheetDialog.dismiss();
            });
            editText.requestFocus();
            Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();
        });
        bottomSheetDialog.show();
    }
    public void updateList(List<DrawingItem> newList) {
        this.drawingList.clear();
        this.drawingList.addAll(newList);
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return drawingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView drawing_name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            drawing_name = itemView.findViewById(R.id.name);
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    public void filterList(String text) {
        drawingList.clear();
        if (text.isEmpty()) {
            drawingList.addAll(fullList);
        } else {
            text = text.toLowerCase();
            for (DrawingItem item : fullList) {
                if (item.getDrawing_name().toLowerCase().contains(text)) {
                    drawingList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }
}
