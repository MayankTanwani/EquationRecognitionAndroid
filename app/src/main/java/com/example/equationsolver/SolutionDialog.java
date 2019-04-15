package com.example.equationsolver;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SolutionDialog extends AppCompatDialogFragment {

    public interface OnPositiveListener {
        void clearArrayList(boolean flag);
    }

    Context context;
    TextView tvGraphUrl, tvEquations, tvSolution;
    ArrayList<Equation> equationArrayList;
    Solution solution;
    OnPositiveListener onPositiveListener;
    public static final String TAG = "DIALOG_SOLUTION";

    public void setEquationsAndSolution(Context context, ArrayList<Equation> equationArrayList, Solution solution) {
        this.context = context;
        this.equationArrayList = equationArrayList;
        this.solution = solution;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_solution, null);

        tvGraphUrl = view.findViewById(R.id.tvGraphUrl);
        tvEquations = view.findViewById(R.id.tvSolutionEquation);
        tvSolution = view.findViewById(R.id.tvSolutionResult);
        // TODO: Insert the graph image here
        Log.d(TAG, "Graph Url: " + solution.getGraph());
        tvGraphUrl.setMovementMethod(LinkMovementMethod.getInstance());
        String url = "<a href='" + solution.getGraph() +"'> Click here to view graph</a>";
        tvGraphUrl.setText(Html.fromHtml(url));
        // Glide.with(context).load(graphUrl).into(ivGraph);
        // Picasso.get().load(solution.getGraph()).into(ivGraph);

        String[] equations = new String[equationArrayList.size()];
        for(int i = 0 ; i < equationArrayList.size() ; i++) {
            equations[i] = equationArrayList.get(i).getEquation();
        }
        StringBuilder b = new StringBuilder();
        for(String s: equations) {
            b.append(s);
            b.append("\n");
        }
        tvEquations.setText(b.toString().trim());
        tvSolution.setText(solution.getResult());

        builder.setView(view)
                .setTitle("Solution")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(onPositiveListener != null) {
                            onPositiveListener.clearArrayList(true);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onPositiveListener = (OnPositiveListener) context;
        } catch (ClassCastException cce) {
            cce.printStackTrace();
        }
    }
}
