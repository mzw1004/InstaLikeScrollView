package me.mazhiwei.demo;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        RecyclerView rv = findViewById(R.id.rv_test);
        rv.setLayoutManager(new GridLayoutManager(this, 3));
        rv.setAdapter(new TestAdapter());
    }

    private class TestAdapter extends RecyclerView.Adapter<ItemHolder> {

        private int[] randomColor = new int[] { Color.CYAN, Color.BLUE, Color.DKGRAY,
                Color.YELLOW, Color.LTGRAY, Color.MAGENTA, Color.RED };

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            SquareTextView textView = new SquareTextView(parent.getContext());
            textView.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            return new ItemHolder(textView);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            ((TextView) holder.itemView).setText("Position " + position);

            holder.itemView.setBackgroundColor(randomColor[position % randomColor.length]);
        }

        @Override
        public int getItemCount() {
            return 60;
        }
    }

    private class ItemHolder extends RecyclerView.ViewHolder {

        public ItemHolder(View itemView) {
            super(itemView);
        }
    }
}
