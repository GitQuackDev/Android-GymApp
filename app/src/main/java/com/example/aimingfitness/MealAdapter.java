package com.example.aimingfitness;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private List<Meal> mealList;
    private Context context;
    private OnMealItemClickListener listener;
    
    // Interface for meal item interactions
    public interface OnMealItemClickListener {
        void onMealClick(Meal meal, int position);
        void onEditClick(Meal meal, int position);
        void onDeleteClick(Meal meal, int position);
    }
    public MealAdapter(Context context, List<Meal> mealList, OnMealItemClickListener listener) {
        this.context = context;
        this.mealList = mealList != null ? mealList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_meal, parent, false);
        return new MealViewHolder(view);
    }    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = mealList.get(position);
        holder.tvMealName.setText(meal.getName());
        holder.tvMealDescription.setText(meal.getDescription()); // Set description
        holder.tvMealCalories.setText(String.format("%.0f Calories", meal.getCalories()));
        holder.tvMealType.setText(meal.getMealType());
        holder.tvProtein.setText(String.format("P: %.0fg", meal.getProtein())); // Set protein
        holder.tvCarbs.setText(String.format("C: %.0fg", meal.getCarbs()));     // Set carbs
        holder.tvFats.setText(String.format("F: %.0fg", meal.getFats()));       // Set fats

        // Load image using Glide if available, otherwise use placeholder
        if (meal.getImageUrl() != null && !meal.getImageUrl().isEmpty()) {
            Glide.with(context)
                .load(meal.getImageUrl())
                .apply(new RequestOptions()
                    .placeholder(R.drawable.ic_placeholder_meal)
                    .error(R.drawable.ic_placeholder_meal))
                .into(holder.ivMealImage);
        } else {
            holder.ivMealImage.setImageResource(R.drawable.ic_placeholder_meal);
        }
        
        // Set item click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMealClick(meal, position);
            }
        });
        
        // Add options menu (3 dots)
        holder.ivMoreOptions.setOnClickListener(v -> {
            if (listener != null) {
                showPopupMenu(holder.ivMoreOptions, meal, position);
            }
        });
    }
    
    private void showPopupMenu(View view, Meal meal, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.meal_item_menu);
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit_meal) {
                if (listener != null) {
                    listener.onEditClick(meal, position);
                }
                return true;
            } else if (itemId == R.id.action_delete_meal) {
                if (listener != null) {
                    listener.onDeleteClick(meal, position);
                }
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public void updateMeals(List<Meal> newMeals) {
        this.mealList.clear();
        if (newMeals != null) {
            this.mealList.addAll(newMeals);
        }
        notifyDataSetChanged(); // Consider using DiffUtil for better performance
    }    static class MealViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMealImage;
        ImageView ivMoreOptions;
        TextView tvMealName;
        TextView tvMealDescription;
        TextView tvMealCalories;
        TextView tvMealType;
        TextView tvProtein;
        TextView tvCarbs;
        TextView tvFats;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMealImage = itemView.findViewById(R.id.ivMealImage);
            ivMoreOptions = itemView.findViewById(R.id.ivMoreOptions);
            tvMealName = itemView.findViewById(R.id.tvMealName);
            tvMealDescription = itemView.findViewById(R.id.tvMealDescription);
            tvMealCalories = itemView.findViewById(R.id.tvMealCalories);
            tvMealType = itemView.findViewById(R.id.tvMealType);
            tvProtein = itemView.findViewById(R.id.tvProtein);
            tvCarbs = itemView.findViewById(R.id.tvCarbs);
            tvFats = itemView.findViewById(R.id.tvFats);
        }
    }
}
