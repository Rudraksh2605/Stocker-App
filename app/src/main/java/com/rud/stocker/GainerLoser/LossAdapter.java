package com.rud.stocker.GainerLoser;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rud.stocker.R;

import java.util.List;

public class LossAdapter extends RecyclerView.Adapter<LossAdapter.ViewHolder> {
    private List<Stock> stockList;

    public LossAdapter(List<Stock> stockList) {
        this.stockList = stockList;
        Log.d("StockList", stockList.toString());
    }

    @NonNull
    @Override
    public LossAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gainer_loser, parent, false);
        return new LossAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LossAdapter.ViewHolder holder, int position) {
        Stock stock = stockList.get(position);
        holder.stockName.setText(stock.getName());
        holder.percentChange.setText(stock.getPercentChange());
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView stockName, percentChange;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stockName = itemView.findViewById(R.id.stock_name);
            percentChange = itemView.findViewById(R.id.percent_change);
        }
    }
}
