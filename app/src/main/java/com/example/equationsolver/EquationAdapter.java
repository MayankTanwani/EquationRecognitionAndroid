package com.example.equationsolver;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class EquationAdapter extends RecyclerView.Adapter<EquationAdapter.EquationViewHolder> {

    public interface OnEquationClickListener {
        void getPosition(int i);
    }

    Context context;
    ArrayList<Equation> equationArrayList;
    OnEquationClickListener onEquationClickListener;

    public EquationAdapter(Context context, ArrayList<Equation> equationArrayList) {
        this.context = context;
        this.equationArrayList = equationArrayList;
    }

    public void updateEquations(ArrayList<Equation> equationArrayList) {
        this.equationArrayList = equationArrayList;
        notifyDataSetChanged();
    }

    public void setOnEquationClickListener(OnEquationClickListener onEquationClickListener) {
        this.onEquationClickListener = onEquationClickListener;
    }

    @NonNull
    @Override
    public EquationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_equation, viewGroup, false);
        return new EquationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final EquationViewHolder equationViewHolder, final int i) {
        Equation equation = equationArrayList.get(i);
        Glide.with(context).load(equation.getImgUri()).centerCrop().into(equationViewHolder.ivEquationImage);
        equationViewHolder.tvStringEquation.setText(equation.getEquation());
        equationViewHolder.etEditEquation.setText(equation.getEquation());
        equationViewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onEquationClickListener != null) {
                    onEquationClickListener.getPosition(i);
                }
            }
        });
        equationViewHolder.tvStringEquation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                equationViewHolder.llEdit.setVisibility(View.VISIBLE);
            }
        });
        equationViewHolder.fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String equationString = equationViewHolder.etEditEquation.getText().toString();
                String url = equationArrayList.get(i).getImgUri();
                equationArrayList.remove(i);
                equationArrayList.add(i, new Equation(url, equationString));
                updateEquations(equationArrayList);
                equationViewHolder.llEdit.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return equationArrayList.size();
    }

    public static class EquationViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llEdit;
        ImageView ivEquationImage;
        TextView tvStringEquation;
        EditText etEditEquation;
        FloatingActionButton fabEdit;
        View rootView;

        public EquationViewHolder(@NonNull View itemView) {
            super(itemView);
            llEdit = itemView.findViewById(R.id.llEdit);
            ivEquationImage = itemView.findViewById(R.id.ivEquationImage);
            tvStringEquation = itemView.findViewById(R.id.tvStringEquation);
            etEditEquation = itemView.findViewById(R.id.etEquation);
            fabEdit = itemView.findViewById(R.id.fabEditEquation);
            rootView = itemView;
        }
    }
}
