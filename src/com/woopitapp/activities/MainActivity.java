package com.woopitapp.activities;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenListener;
import com.suredigit.inappfeedback.FeedbackDialog;
import com.woopitapp.R;
import com.woopitapp.WoopitFragmentActivity;
import com.woopitapp.entities.User;
import com.woopitapp.fragments.FriendsFragment;
import com.woopitapp.fragments.HomeFragment;
import com.woopitapp.fragments.ModelsFragment;
import com.woopitapp.server_connections.InsertCoins;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.TabPager;
import com.woopitapp.services.Utils;
import com.woopitapp.services.WoopitService;
import com.woopitapp.services.WoopitService.LocalBinder;

public class MainActivity extends WoopitFragmentActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
 
    private TabHost mTabHost;
    private ViewPager mViewPager;
    private TabPager mPagerAdapter;
	SlidingMenu menu;
	private int SHARE_REQUEST_CODE = 1;
    
    // Service
    WoopitService wService;
    boolean mBound;    
	
	// Broadcast receivers
	FriendsUpdateReceiver f_receiver;
	ModelPurchaseReceiver m_receiver;
	ProfileChangeReceiver p_receiver;
	MessagesUpdateReceiver mu_receiver;
	
    
	// Feedback
	private FeedbackDialog feedBack;
	
	/* Google cloud messaging */
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private final String GCM_TAG = "GCM";
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String FIRST_TIME_PREFERENCE = "com.woopitapp.first_time";
    String SENDER_ID = "719506420236";
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    String regid;
    Activity act;
    boolean share_launched = false , share_clicked = false;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        initialiseTabHost(savedInstanceState);
        act = this;
        /*
        RelativeLayout main_layout = (RelativeLayout) findViewById(R.id.main_layout);
        main_layout.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				
				if ( ((LinearLayout)findViewById(R.id.welcome_tip_home)).getVisibility() == View.VISIBLE ){
		    		((LinearLayout)findViewById(R.id.welcome_tip_home)).setVisibility(View.GONE);
		    		return true;
		    	}
		    	
		    	return false;
			}
		});*/
        
        SharedPreferences sp = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
    	editor.putBoolean(FIRST_TIME_PREFERENCE, true);
    	editor.commit();
        
        boolean first_time = sp.getBoolean(FIRST_TIME_PREFERENCE, true);
        
        if ( first_time ){
        	((LinearLayout)findViewById(R.id.welcome_tip_home)).setVisibility(View.VISIBLE);
        	//SharedPreferences.Editor editor = sp.edit();
        	//editor.putBoolean(FIRST_TIME_PREFERENCE, false);
        	//editor.commit();
        }        
        
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
        
        /* Recibe cambios en perfil */
		p_receiver = new ProfileChangeReceiver();
        registerReceiver(p_receiver,new IntentFilter(this.getString(R.string.broadcast_profile_update)));
        
        /* Recibe cambios en mensajes */
        mu_receiver = new MessagesUpdateReceiver();
        registerReceiver(mu_receiver,new IntentFilter(this.getString(R.string.broadcast_messages)));

        // Check device for Play Services APK.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(getApplicationContext());

            if (regid.equals("")) {
                registerInBackground();
            }
        } else {
            Log.i(GCM_TAG, "No valid Google Play Services APK found.");
        }
                        
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if ( requestCode == SHARE_REQUEST_CODE && share_launched && share_clicked ){
			
			Utils.onShareWoopit(getApplicationContext(), "SlidingMenu", "Compartido");
			new InsertCoins(act , 1 , R.string.por_compartir ).execute();
		}
		if(requestCode == HomeFragment.REQUEST_MESSAGE){
    		if(resultCode == this.RESULT_OK){
    			 Intent i = new Intent(getApplicationContext(),MapUnMessageActivity.class);
				 Bundle extras = data.getExtras();
				 double latitud = extras.getDouble("latitud");
				 double longitud  = extras.getDouble("longitud");
				 String nombre = extras.getString("nombre");
    			 i.putExtra("latitud",latitud);
				 i.putExtra("longitud",longitud);
				 i.putExtra("nombre", nombre);
				 startActivity(i);
    		}
    	}
		share_launched = false;
		share_clicked = false;
	}
    
    public void onStart(){
    	super.onStart();
    	
    	Intent intent = new Intent(this, WoopitService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    
    protected void onResume(){
    	super.onResume();
        checkPlayServices();
    }
    
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }
    
    protected void onPause(){
    	super.onPause();
    	feedBack.dismiss();
    }
    
    public void onStop(){
    	super.onStop();
    	
    	if ( share_launched ){
    		share_clicked = true;
    	}
    	
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
    	
    	if ( mu_receiver != null ){
    		unregisterReceiver(mu_receiver);
    	}
    	
    	if ( mBound ){
    		unbindService(mConnection);
    	}
    	
    }
        
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
    	
    	boolean ret = super.dispatchTouchEvent(event);
    	
    	if ( ((LinearLayout)findViewById(R.id.welcome_tip_home)).getVisibility() == View.VISIBLE ){
    		
    		Animation fadeOut = new AlphaAnimation(1, 0);
    		fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
    		fadeOut.setDuration(400);
    		fadeOut.setAnimationListener(new AnimationListener(){

				@Override
				public void onAnimationEnd(Animation animation) {
					((LinearLayout)findViewById(R.id.welcome_tip_home)).setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {}

				@Override
				public void onAnimationStart(Animation animation) {}
			});
    		
    		((LinearLayout)findViewById(R.id.welcome_tip_home)).startAnimation(fadeOut);
    		
    		//((LinearLayout)findViewById(R.id.welcome_tip_home)).setVisibility(View.GONE);
    		//return true;
    	}
    	
    	return ret;
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
        menu.setOnOpenListener(new OnOpenListener(){

			@Override
			public void onOpen() {
				
		        TextView tip = (TextView ) menu.findViewById(R.id.tips);
				int tip_number = new Random().nextInt(14-1) + 1;
		        
		        switch( tip_number ){
		        
		        case 1:
		        	tip.setText(R.string.tip1);
		        	break;
		        case 2:
		        	tip.setText(R.string.tip2);
		        	break;
		        case 3:
		        	tip.setText(R.string.tip3);
		        	break;
		        case 4:
		        	tip.setText(R.string.tip4);
		        	break;
		        case 5:
		        	tip.setText(R.string.tip5);
		        	break;
		        case 6:
		        	tip.setText(R.string.tip6);
		        	break;
		        case 7:
		        	tip.setText(R.string.tip7);
		        	break;
		        case 8:
		        	tip.setText(R.string.tip8);
		        	break;
		        case 9:
		        	tip.setText(R.string.tip9);
		        	break;
		        case 10:
		        	tip.setText(R.string.tip10);
		        	break;
		        case 11:
		        	tip.setText(R.string.tip11);
		        	break;
		        case 12:
		        	tip.setText(R.string.tip12);
		        	break;
		        case 13:
		        	tip.setText(R.string.tip13);
		        	break;
		        default:
		        	tip.setText(R.string.tip1);
		        	break;
		        }
			}
		});
        
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
    	
    	Utils.onEditProfileEnter(getApplicationContext(), "SlidingMenu");
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
		
		startActivityForResult(Intent.createChooser(sendIntent, getResources().getString(R.string.compartir_woopit)),SHARE_REQUEST_CODE);
		
		Utils.onShareWoopit(getApplicationContext(), "SlidingMenu", "Entrar");
		share_launched = true;
		
	}
    
    public void sendFeedback( View v ){
    	
    	Utils.onSendFeedback(getApplicationContext(), "SlidingMenu", "Entrar");
    	
    	feedBack.show();
    	toggleSlidingMenu(v);
    }

    public void goBuyCoins( View v ){
    	
    	Intent i = new Intent(this,BuyCoinActivity.class);
    	
    	i.putExtra("modelId", -1);    	
    	startActivity(i);
    	
    	Utils.onCoinsEnter(getApplicationContext(), "SlidingMenu");
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

    public class MessagesUpdateReceiver extends BroadcastReceiver {
        
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		
        	HomeFragment fragment = (HomeFragment) mPagerAdapter.getItem(0);
    		
        	if ( fragment.isVisible() ){
        		fragment.updateContent();
        	}
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
    
    /* Google cloud messaging */
    
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(GCM_TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }
    
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.equals("")) {
            Log.i(GCM_TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(GCM_TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    
    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(MainActivity.class.getSimpleName(),Context.MODE_PRIVATE);
    }
    
    private static int getAppVersion(Context context) {
        
    	try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    
    private void registerInBackground() {
    	
    	new AsyncTask<Void,Void,String>(){
    		
			@Override
			protected String doInBackground(Void... arg0) {
				String msg = "";
	            try {
	                if (gcm == null) {
	                    gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
	                }
	                regid = gcm.register(SENDER_ID);
	                msg = "Device registered, registration ID=" + regid;

	                // You should send the registration ID to your server over HTTP,
	                // so it can use GCM/HTTP or CCS to send messages to your app.
	                // The request to your server should be authenticated if your app
	                // is using accounts.
	                sendRegistrationIdToBackend(regid);

	                // For this demo: we don't need to send it because the device
	                // will send upstream messages to a server that echo back the
	                // message using the 'from' address in the message.

	                // Persist the regID - no need to register again.
	                storeRegistrationId(getApplicationContext(), regid);
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	                // If there is an error, don't just keep trying to register.
	                // Require the user to click a button again, or perform
	                // exponential back-off.
	            }
	            return msg;
			}
			
			@Override
	        protected void onPostExecute(String msg) {
	            Log.i(GCM_TAG,msg + "\n");
	        }
    		
    	}.execute();
    	
    }
    
    private void sendRegistrationIdToBackend( String regid ) {
        new SetGcmId( regid  ).execute();
    }
    
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(GCM_TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
    
    class SetGcmId extends ServerConnection{
    	
    	String gcm_id;
    	
    	public SetGcmId( String gcm_id ){
			super();
			
			this.gcm_id = gcm_id;
			init(getApplicationContext(),"set_gcm_id", new Object[]{ User.get(getApplicationContext()).id , gcm_id });
    	}
    	
		@Override
		public void onComplete(String result) {
			
			if ( result != null ){
				
				if ( result.equals("ok")){
					storeRegistrationId(getApplicationContext(),gcm_id);
					Log.i(GCM_TAG, "Registration successful");
				}
				else{
					Toast.makeText(getApplicationContext(), R.string.error_desconocido, Toast.LENGTH_SHORT).show();
				}
			}
			else{
				Toast.makeText(getApplicationContext(), R.string.error_de_conexion, Toast.LENGTH_SHORT).show();				
			}
			
		}
    	
    }
    
}

