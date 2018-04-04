package com.sjsu.se195.uniride;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;
import java.util.ArrayList;

import devlight.io.library.ntb.NavigationTabBar;

/**
 * Created by akshat on 3/31/18.
 */

public class NavBar{
    private Activity activity;
    private NavigationTabBar navigationTabBar;
    private static NavBar instance = null;
    private boolean initialized = false;
    private int INDEX;
    Runnable mDelay;
    Handler mHandler;

    private NavBar(){
    }

    public static NavBar getInstance(){
        if(instance == null){
            instance = new NavBar();
        }
        return instance;
    }

    public void initializeTab(Activity a){
        this.activity = a;
        navigationTabBar = (NavigationTabBar) activity.findViewById(R.id.ntb);

        ViewPager viewPager = (ViewPager) activity.findViewById(R.id.vp_horizontal_ntb);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view.equals(object);
            }

            @Override
            public void destroyItem(final View container, final int position, final Object object) {
                ((ViewPager) container).removeView((View) object);
            }

            @Override
            public Object instantiateItem(final ViewGroup container, final int position) {
                return null;
            }
        });

        final String[] colors = activity.getResources().getStringArray(R.array.ntb_colors);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(activity.getResources().getDrawable(R.drawable.ic_home_white_24dp), Color.parseColor(colors[0])).title("Heart").badgeTitle("NTB").build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(activity.getResources().getDrawable(R.drawable.ic_person_white_24dp),Color.parseColor(colors[1])).title("Cup").badgeTitle("with").build()
        );
        models.add(new NavigationTabBar.Model.Builder(activity.getResources().getDrawable(R.drawable.ic_account_balance_white_24dp),Color.parseColor(colors[2])).title("Diploma").badgeTitle("state").build()
        );

        navigationTabBar.setModels(models);
        if(activity instanceof MainActivity) {
            navigationTabBar.setViewPager(viewPager, 0);
        }else if(activity instanceof ShowOrganizationsActivity){
            navigationTabBar.setViewPager(viewPager, 2);
        }

        navigationTabBar.setTitleMode(NavigationTabBar.TitleMode.ACTIVE);
        navigationTabBar.setBadgeGravity(NavigationTabBar.BadgeGravity.BOTTOM);
        navigationTabBar.setBadgePosition(NavigationTabBar.BadgePosition.CENTER);
        navigationTabBar.setTypeface("fonts/custom_font.ttf");
        navigationTabBar.setIsBadged(true);
        navigationTabBar.setIsTitled(true);
        navigationTabBar.setIsTinted(true);
        navigationTabBar.setIsBadgeUseTypeface(true);
        navigationTabBar.setBadgeBgColor(Color.RED);
        navigationTabBar.setBadgeTitleColor(Color.WHITE);
        navigationTabBar.setIsSwiped(true);
        navigationTabBar.setBadgeSize(10);
        navigationTabBar.setTitleSize(10);
        navigationTabBar.setIconSizeFraction(0.5f);
    }

    public void tabListener(final Activity current){
        mHandler = new Handler();

        mDelay = new Runnable() {
            @Override
            public void run() {
                int index = NavBar.this.INDEX;
                if(index==0 && !(current instanceof MainActivity)){
                    Intent intent = new Intent(current, MainActivity.class);
                    current.startActivity(intent);
                }
                if(index==2 && !(current instanceof ShowOrganizationsActivity)){
                    Intent intent = new Intent(current, ShowOrganizationsActivity.class);
                    current.startActivity(intent);
                }
            }
        };

        navigationTabBar.setOnTabBarSelectedIndexListener(new NavigationTabBar.OnTabBarSelectedIndexListener() {
            @Override
            public void onStartTabSelected(NavigationTabBar.Model model, int index) {
                NavBar.this.INDEX = index;
                System.out.println(index);
                mHandler.postDelayed(mDelay, 250);
            }

            @Override
            public void onEndTabSelected(NavigationTabBar.Model model, int index) {

            }
        });
    }
}
