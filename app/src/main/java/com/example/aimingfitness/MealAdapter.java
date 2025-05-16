package com.example.aimingfitness;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

// TODO: Consider using a library like Glide or Picasso for image loading from URLs

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private List<Meal> mealList;
    private Context context;

    public MealAdapter(Context context, List<Meal> mealList) {
        this.context = context;
        this.mealList = mealList != null ? mealList : new ArrayList<>();
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = mealList.get(position);
        holder.tvMealName.setText(meal.getName());
        holder.tvMealDescription.setText(meal.getDescription()); // Set description
        holder.tvMealCalories.setText(String.format("%.0f Calories", meal.getCalories()));
        holder.tvMealType.setText(meal.getMealType());
        holder.tvProtein.setText(String.format("P: %.0fg", meal.getProtein())); // Set protein
        holder.tvCarbs.setText(String.format("C: %.0fg", meal.getCarbs()));     // Set carbs
        holder.tvFats.setText(String.format("F: %.0fg", meal.getFats()));       // Set fats

        // For now, using a placeholder. Later, load image from meal.getImageUrl()
        // using a library like Glide or Picasso.
        // e.g., Glide.with(context).load(meal.getImageUrl()).placeholder(R.drawable.ic_placeholder_meal).into(holder.ivMealImage);
        holder.ivMealImage.setImageResource(R.drawable.ic_placeholder_meal);
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
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMealImage;
        TextView tvMealName;
        TextView tvMealDescription; // Added
        TextView tvMealCalories;
        TextView tvMealType;
        TextView tvProtein;         // Added
        TextView tvCarbs;           // Added
        TextView tvFats;            // Added

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMealImage = itemView.findViewById(R.id.ivMealImage);
            tvMealName = itemView.findViewById(R.id.tvMealName);
            tvMealDescription = itemView.findViewById(R.id.tvMealDescription); // Added
            tvMealCalories = itemView.findViewById(R.id.tvMealCalories);
            tvMealType = itemView.findViewById(R.id.tvMealType);
            tvProtein = itemView.findViewById(R.id.tvProtein);                 // Added
            tvCarbs = itemView.findViewById(R.id.tvCarbs);                   // Added
            tvFats = itemView.findViewById(R.id.tvFats);                     // Added
        }
    }
}
