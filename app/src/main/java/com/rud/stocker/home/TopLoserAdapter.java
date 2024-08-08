package com.rud.stocker.home;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rud.stocker.R;

import java.util.List;

public class TopLoserAdapter extends RecyclerView.Adapter<TopLoserAdapter.ViewHolder> {

    private List<TopGainer.StockChange> stockChanges;

    public void setStockChanges(List<TopGainer.StockChange> stockChanges) {
        this.stockChanges = stockChanges;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TopGainer.StockChange stockChange = stockChanges.get(position);
        holder.stockName.setText(stockChange.getSymbol());
        holder.stockPrice.setText(String.valueOf(stockChange.getCurrentPrice()));
        holder.percentageGain.setText(String.format("%s%%", stockChange.getPercentageChange()));
    }

    @Override
    public int getItemCount() {
        return stockChanges != null ? stockChanges.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView stockName;
        TextView stockPrice;
        TextView percentageGain;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stockName = itemView.findViewById(R.id.stock_name);
            stockPrice = itemView.findViewById(R.id.stock_price);
            percentageGain = itemView.findViewById(R.id.percentage_gain);
        }
    }
}

