package com.woopitapp.activities;

import java.util.List;
import java.util.Vector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.woopitapp.R;

import com.woopitapp.entities.User;
import com.woopitapp.fragments.FriendsFragment;
import com.woopitapp.fragments.HomeFragment;
import com.woopitapp.fragments.ModelsFragment;
import com.woopitapp.services.TabPager;
import com.woopitapp.services.Utils;

public class MainActivity extends FragmentActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
 
    private TabHost mTabHost;
    private ViewPager mViewPager;
    private TabPager mPagerAdapter;
    
	SlidingMenu menu;
	
	// Broadcast receivers
	FriendsUpdateReceiver f_receiver;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        initialiseTabHost(savedInstanceState);
        
        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
        }
        
        intialiseViewPager();
        setSlidingMenu();
        initMessageButton();
        
        /* Recibe cambios en lista de amigos */
        f_receiver = new FriendsUpdateReceiver();
        registerReceiver(f_receiver,new IntentFilter(this.getString(R.string.broadcast_friends_list)));
        
    }
    
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("tab", mTabHost.getCurrentTabTag());
        super.onSaveInstanceState(outState);
    }
    
    protected void onDestroy(){
    	super.onDestroy();
    	
    	if ( f_receiver != null ){
    		unregisterReceiver(f_receiver);
    	}
    }
    
    /* Tabs */
    private class TabInfo {
         private String tag;
         private Class<?> clss;
         private Bundle args;
         private Fragment fragment;
         TabInfo(String tag, Class<?> class_, Bundle args) {
             this.tag = tag;
             this.clss = class_;
             this.args = args;
         }
 
    }
   
    public class TabFactory implements TabContentFactory {
 
        private final Context mContext;
 
        public TabFactory(Context context) {
            mContext = context;
        }
 
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
 
    }
    
    private void initMessageButton(){
    	ImageView bMessage = (ImageView) findViewById(R.id.new_message);
    	bMessage.setOnClickListener(new  View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent newMessagei =  new  Intent(v.getContext(),MessageActivity.class);
				startActivity(newMessagei);
			}
		});
    	
    }
    
    private void intialiseViewPager() {
 
        List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(Fragment.instantiate(this, HomeFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, FriendsFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, ModelsFragment.class.getName()));
        mPagerAdapter  = new TabPager(getSupportFragmentManager(), fragments);
        
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(this);
    }
    
    private void initialiseTabHost( final Bundle args) {
    	
        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();
        
        // Home tab
        View homeView = createTabView(mTabHost.getContext(), 1 );
        final TabSpec tabHome = mTabHost.newTabSpec("Home").setIndicator(homeView);
        
        // Friends tab
        View friendsView = createTabView(mTabHost.getContext(), 2 );
        final TabSpec tabFriends = mTabHost.newTabSpec("Friends").setIndicator(friendsView);
        
        // Model tab
        View modelView = createTabView(mTabHost.getContext(), 3 );
        final TabSpec tabModel = mTabHost.newTabSpec("Models").setIndicator(modelView);
        
        // Profile tab
        View profileView = createTabView(mTabHost.getContext(), 4 );
        final TabSpec tabProfile = mTabHost.newTabSpec("Profile").setIndicator(profileView);
        
        AddTab(this, mTabHost, tabHome, ( new TabInfo("Home", HomeFragment.class, args)));
        AddTab(this, mTabHost, tabFriends, ( new TabInfo("Friends", FriendsFragment.class, args)));
        AddTab(this, mTabHost, tabModel, ( new TabInfo("Models", ModelsFragment.class, args)));       
	         
        mTabHost.setOnTabChangedListener(this);
    }
    
    private static View createTabView(final Context context, final int tab ) {
    	
    	View view = LayoutInflater.from(context).inflate(R.layout.home_tab, null);
    	ImageView iv = (ImageView) view.findViewById(R.id.tab_image);
    	
    	switch( tab ){
    	
    	case 1:
        	iv.setImageResource(R.drawable.home_tab_image);
    		break;
    	case 2:
         	iv.setImageResource(R.drawable.friends_tab_image);
    		break;
    	case 3:
        	iv.setImageResource(R.drawable.models_tab_image);
    		break;
    	case 4:
        	iv.setImageResource(R.drawable.profile_tab_image);
    		break;
    	
    	}
    	
    	return view;
    }

    private static void AddTab(MainActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) {
    	
        tabSpec.setContent(activity.new TabFactory(activity));
        tabHost.addTab(tabSpec);
    }
    
    public void onTabChanged(String tag) {
    	
        int pos = this.mTabHost.getCurrentTab();
        
        if ( mViewPager == null )
        	return;
        	
        mViewPager.setCurrentItem(pos);
        
        if ( pos == 0 ){
        	mPagerAdapter.notifyDataSetChanged();
        }
    }
 
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
    
    @Override
    public void onPageSelected(int position) {
        this.mTabHost.setCurrentTab(position);
    }
 
    @Override
    public void onPageScrollStateChanged(int state) {}
    
    /* Sliding Menu */
    
    public void setSlidingMenu(){
    	
        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        //menu.setShadowWidth(Utils.dpToPx(30, getApplicationContext()));
        //menu.setShadowDrawable(R.drawable.menu_shadow);
        menu.setBehindWidth(Utils.dpToPx(250, getApplicationContext()));
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.sliding_menu);
        
        User u = User.get(this);
        
        TextView name = (TextView ) menu.findViewById(R.id.name);
        TextView username = (TextView ) menu.findViewById(R.id.username);
        ImageView image = (ImageView ) menu.findViewById(R.id.image);
        
        name.setText(u.name);
        username.setText("@"+u.username);
        Utils.setUserImage(getApplicationContext(), image, u.id);
    }
 
    public void toggleSlidingMenu( View v ){
    	
    	if ( menu.isMenuShowing() ){
    		menu.showContent();
    	}
    	else{
    		menu.showMenu();
    	}
    	
    }
    
    /* Ir a encontrar amigos */
    public void goFindFriends( View v ){
    	
    	Intent i = new Intent(this,FindFriendsActivity.class);
    	startActivity(i);    	
    }
        
    public void goEditProfile( View v ){
    	
    }
    
    public void goMyProfile( View v ){
    	
    	Intent i = new Intent(this,ProfileActivity.class);
    	
    	i.putExtra("id_user", User.get(getApplicationContext()).id);
    	
    	startActivity(i);
    }
    
    /* Broadcasts receivers */
    public class FriendsUpdateReceiver extends BroadcastReceiver {
        
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		
        	FriendsFragment fragment = (FriendsFragment) mPagerAdapter.getItem(1);
    		
        	if ( fragment.isVisible() ){
        		fragment.updateContent();
        	}
    	}
      
    }
        
    
}

