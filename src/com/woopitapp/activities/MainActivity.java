package com.woopitapp.activities;

import java.util.List;
import java.util.Vector;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.suredigit.inappfeedback.FeedbackDialog;
import com.woopitapp.R;
import com.woopitapp.WoopitFragmentActivity;
import com.woopitapp.entities.User;
import com.woopitapp.fragments.FriendsFragment;
import com.woopitapp.fragments.HomeFragment;
import com.woopitapp.fragments.ModelsFragment;
import com.woopitapp.services.TabPager;
import com.woopitapp.services.Utils;
import com.woopitapp.services.WoopitService;
import com.woopitapp.services.WoopitService.LocalBinder;

public class MainActivity extends WoopitFragmentActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
 
    private TabHost mTabHost;
    private ViewPager mViewPager;
    private TabPager mPagerAdapter;
	SlidingMenu menu;
    
    // Service
    WoopitService wService;
    boolean mBound;    
	
	// Broadcast receivers
	FriendsUpdateReceiver f_receiver;
	ModelPurchaseReceiver m_receiver;
	ProfileChangeReceiver p_receiver;
    
	// Feedback
	private FeedbackDialog feedBack;
	
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        initialiseTabHost(savedInstanceState);
        
        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
        }
        
        intialiseViewPager();
        setSlidingMenu();

        feedBack = Utils.getFeedbackDialog(this);
        
        /* Recibe cambios en lista de amigos */
        f_receiver = new FriendsUpdateReceiver();
        registerReceiver(f_receiver,new IntentFilter(this.getString(R.string.broadcast_friends_list)));

        /* Recibe si compraste un modelo */
        m_receiver = new ModelPurchaseReceiver();
        registerReceiver(m_receiver,new IntentFilter(this.getString(R.string.broadcast_model_purchase)));
        
        /* Recibe cambios en lista de amigos */
		p_receiver = new ProfileChangeReceiver();
        registerReceiver(p_receiver,new IntentFilter(this.getString(R.string.broadcast_profile_update)));
        
    }
    
    protected void onStart(){
    	super.onStart();
    	
    	Intent intent = new Intent(this, WoopitService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }
    
    protected void onPause(){
    	super.onPause();
    	feedBack.dismiss();
    }
    
    protected void onDestroy(){
    	super.onDestroy();
    	
    	if ( f_receiver != null ){
    		unregisterReceiver(f_receiver);
    	}
    	
    	if ( m_receiver != null ){
    		unregisterReceiver(m_receiver);
    	}
    	
    	if ( p_receiver != null ){
    		unregisterReceiver(p_receiver);
    	}
    	
    	if ( mBound ){
    		unbindService(mConnection);
    	}
    	
    }
    
    public void goTest( View v ){
    	
    	Intent i = new Intent(this,TestActivity.class);    	 	
    	startActivity(i);
    }
    
    /* Tabs */

    public void setActualTab(int id){
    	Log.e("ID", id+"");
    	mTabHost.setCurrentTab(id);
    }
    
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
        
        AddTab(this, mTabHost, tabHome, ( new TabInfo("Home", HomeFragment.class, args)));
        AddTab(this, mTabHost, tabFriends, ( new TabInfo("Friends", FriendsFragment.class, args)));
        AddTab(this, mTabHost, tabModel, ( new TabInfo("Models", ModelsFragment.class, args)));       
	         
        mTabHost.setOnTabChangedListener(this);
    }
    
    private View createTabView(final Context context, final int tab ) {
    	
    	TabWidget tw = (TabWidget) findViewById(android.R.id.tabs);
    	View view = LayoutInflater.from(this).inflate(R.layout.home_tab, tw,false);
    	
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
        menu.setShadowWidth(Utils.dpToPx(10, getApplicationContext()));
        menu.setShadowDrawable(R.drawable.menu_shadow);
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
    
    public void goEditProfile( View v ){
    	Intent i = new Intent(this,EditProfileActivity.class);    	
    	startActivity(i);
    }
    
    public void goMyProfile( View v ){
    	
    	Intent i = new Intent(this,ProfileActivity.class);
    	
    	i.putExtra("id_user", User.get(getApplicationContext()).id);    	
    	startActivity(i);
    }
    
    public void shareWoopit( View v ){
		
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.compartir_woopit_texto));
		sendIntent.setType("text/plain");
		
		startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.compartir_woopit)));
	}
    
    public void sendFeedback( View v ){
    	feedBack.show();
    	toggleSlidingMenu(v);
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
    
    public class ModelPurchaseReceiver extends BroadcastReceiver {
        
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		
    		ModelsFragment fragment = (ModelsFragment) mPagerAdapter.getItem(2);
    		
        	if ( fragment.isVisible() ){
        		fragment.invalidateModels();
        	}
    	}
      
    }
    
    public class ProfileChangeReceiver extends BroadcastReceiver {
        
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		
    		User u = User.get(context);
            
            TextView name = (TextView ) menu.findViewById(R.id.name);
            TextView username = (TextView ) menu.findViewById(R.id.username);
            ImageView image = (ImageView ) menu.findViewById(R.id.image);
            
            name.setText(u.name);
            username.setText("@"+u.username);
            Utils.setUserImage(getApplicationContext(), image, u.id);
    	}
      
    }

    /* Service connection */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
        	
            LocalBinder binder = (LocalBinder) service;
            wService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };    
    
}

