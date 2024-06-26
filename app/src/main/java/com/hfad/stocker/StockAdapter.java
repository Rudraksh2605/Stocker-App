package com.hfad.stocker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {
    private List<TopGainer.StockChange> stockChanges;

    public StockAdapter() {
        this.stockChanges = new ArrayList<>(); // Initialize with an empty list
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_card, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        TopGainer.StockChange stockChange = stockChanges.get(position);
        holder.stockName.setText(stockChange.getStock().getSymbol());
        holder.stockPrice.setText(stockChange.getStock().getCurrentPrice());
        holder.percentageGain.setText(String.format("%.2f%%", stockChange.getPercentageChange()));
    }

    @Override
    public int getItemCount() {
        return stockChanges.size();
    }

    public void setStockChanges(List<TopGainer.StockChange> stockChanges) {
        this.stockChanges = stockChanges;
        notifyDataSetChanged();
    }

    static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView stockName;
        TextView stockPrice;
        TextView percentageGain;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            stockName = itemView.findViewById(R.id.stock_name);
            stockPrice = itemView.findViewById(R.id.stock_price);
            percentageGain = itemView.findViewById(R.id.percentage_gain);
        }
    }
}
