package com.rud.stocker.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rud.stocker.R;

import java.util.List;

public class LossStockAdapter extends RecyclerView.Adapter<LossStockAdapter.StockViewHolder> {

    private List<Stock> stockList;

    public LossStockAdapter(List<Stock> stockList) {
        this.stockList = stockList;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_card, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock stock = stockList.get(position);
        holder.stockName.setText(stock.getStockName());
        holder.percentageGain.setText(stock.getPercentageGain());
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView stockName, percentageGain;

        StockViewHolder(View itemView) {
            super(itemView);
            stockName = itemView.findViewById(R.id.stock_name);
            percentageGain = itemView.findViewById(R.id.percentage_gain);
        }
    }
}
