package com.test.li182.my_game;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        Button buttonPK = findViewById(R.id.button_pk);
        Button buttonUnion = findViewById(R.id.button_union);

        buttonPK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectActivity.this, PKActivity.class);
                startActivity(intent);
            }
        });

        buttonUnion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectActivity.this, UnionActivity.class);
                startActivity(intent);
            }
        });
    }
}
