package com.woopitapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.woopitapp.R;
import com.woopitapp.activities.TestActivity;

public class HomeFragment extends Fragment {
		
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
        View view = (LinearLayout)inflater.inflate(R.layout.home_fragment, container, false);
    	
        Button boton = (Button) view.findViewById(R.id.boton_prueba);
        boton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				Intent i = new Intent(getActivity(),TestActivity.class);
				startActivity(i);				
			}
		});
        
        return view;
    }

}
        
