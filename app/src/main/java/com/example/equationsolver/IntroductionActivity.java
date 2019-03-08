package com.example.equationsolver;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IntroductionActivity extends AppCompatActivity {

    ViewPager viewPager;
    IntroPagerAdapter introPagerAdapter;
    TextView[] mDots;
    LinearLayout mDotLayout;
    Button btnNext, btnBack;
    int mCurrentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);

        viewPager = findViewById(R.id.vpIntroduction);
        mDotLayout = findViewById(R.id.llDotLayout);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCurrentPage == mDots.length - 1) {
                    startActivity(new Intent(IntroductionActivity.this, MainActivity.class));
                }
                viewPager.setCurrentItem(mCurrentPage + 1);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(mCurrentPage - 1);
            }
        });

        introPagerAdapter = new IntroPagerAdapter(getSupportFragmentManager(), 3);
        viewPager.setAdapter(introPagerAdapter);
        viewPager.addOnPageChangeListener(pageChangeListener);

        addDotIndicator(0);
    }

    private void addDotIndicator(int position) {
        mDots = new TextView[3];
        mDotLayout.removeAllViews();
        for(int i = 0 ; i < mDots.length ; i++) {
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226;"));
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(getResources().getColor(R.color.colorTransparentWhite));
            mDotLayout.addView(mDots[i]);
        }
        if(mDots.length > 0) {
            mDots[position].setTextColor(getResources().getColor(R.color.colorWhite));
        }
    }

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            addDotIndicator(i);
            mCurrentPage = i;
            if(i == 0) {
                btnNext.setEnabled(true);
                btnBack.setEnabled(false);
                btnBack.setVisibility(View.GONE);
                btnNext.setText("Next");
                btnBack.setText("");
            }
            else if(i == mDots.length - 1) {
                btnNext.setEnabled(true);
                btnBack.setEnabled(true);
                btnBack.setVisibility(View.VISIBLE);
                btnNext.setText("Finish");
                btnBack.setText("Back");
            }
            else {
                btnNext.setEnabled(true);
                btnBack.setEnabled(true);
                btnBack.setVisibility(View.VISIBLE);
                btnNext.setText("Next");
                btnBack.setText("Back");
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };
}
