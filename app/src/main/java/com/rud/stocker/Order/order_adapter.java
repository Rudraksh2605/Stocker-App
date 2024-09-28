package com.rud.stocker.Order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rud.stocker.R;

import java.util.List;

public class order_adapter extends RecyclerView.Adapter<order_adapter.ViewHolder> {

    private List<order_item> orderItems;

    public order_adapter(List<order_item> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        order_item orderItem = orderItems.get(position);
        holder.symbolTextView.setText(orderItem.getSymbol());
        holder.quantityTextView.setText("Quantity: " + orderItem.getQuantity());
        holder.totalAmountTextView.setText("Total Amount: ₹" + orderItem.getTotalAmount());
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView symbolTextView, quantityTextView, totalAmountTextView;

        ViewHolder(View itemView) {
            super(itemView);
            symbolTextView = itemView.findViewById(R.id.symbol);
            quantityTextView = itemView.findViewById(R.id.quantity);
            totalAmountTextView = itemView.findViewById(R.id.total_amount);
        }
    }
}
