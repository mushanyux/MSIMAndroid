package com.chat.uikit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.chat.base.adapter.MSFragmentStateAdapter;
import com.chat.base.base.MSBaseActivity;
import com.chat.base.common.MSCommonModel;
import com.chat.base.config.MSConfig;
import com.chat.base.config.MSConstants;
import com.chat.base.config.MSSharedPreferencesUtil;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.MailListDot;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.ui.Theme;
import com.chat.base.ui.components.CounterView;
import com.chat.base.utils.ActManagerUtils;
import com.chat.base.utils.LayoutHelper;
import com.chat.base.utils.MSDeviceUtils;
import com.chat.base.utils.MSDialogUtils;
import com.chat.base.utils.MSReader;
import com.chat.base.utils.MSTimeUtils;
import com.chat.base.utils.language.MSMultiLanguageUtil;
import com.chat.base.utils.rxpermissions.RxPermissions;
import com.chat.uikit.contacts.service.FriendModel;
import com.chat.uikit.databinding.ActTabMainBinding;
import com.chat.uikit.fragment.ChatFragment;
import com.chat.uikit.fragment.ContactsFragment;
import com.chat.uikit.fragment.MyFragment;
import com.chat.uikit.user.service.UserModel;

import org.telegram.ui.Components.RLottieImageView;

import java.util.ArrayList;
import java.util.List;


/**
 * tab导航栏
 */
public class TabActivity extends MSBaseActivity<ActTabMainBinding> {
    CounterView msgCounterView;
    CounterView contactsCounterView;
    //    CounterView workplaceCounterView;
    View contactsSpotView;
    RLottieImageView chatIV, contactsIV, meIV;
    private TextView chatTV, contactsTV, meTV;
    private long lastClickChatTabTime = 0L;
    private final boolean isShowTabText = true;

    @Override
    protected ActTabMainBinding getViewBinding() {
        return ActTabMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initPresenter() {
        ActManagerUtils.getInstance().clearAllActivity();
    }

    @Override
    public boolean supportSlideBack() {
        return false;
    }

    @SuppressLint("CheckResult")
    @Override
    protected void initView() {
//        msVBinding.vp.setUserInputEnabled(false);
        UserModel.getInstance().device();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            String desc = String.format(getString(R.string.notification_permissions_desc), getString(R.string.app_name));
            RxPermissions rxPermissions = new RxPermissions(this);
            rxPermissions.request(Manifest.permission.POST_NOTIFICATIONS).subscribe(aBoolean -> {
                if (!aBoolean) {
                    MSDialogUtils.getInstance().showDialog(this, getString(com.chat.base.R.string.authorization_request), desc, true, getString(R.string.cancel), getString(R.string.to_set), 0, Theme.colorAccount, index -> {
                        if (index == 1) {
                            EndpointManager.getInstance().invoke("show_open_notification_dialog", this);
                        }
                    });
                }
            });
        } else {
            boolean isEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled();
            if (!isEnabled) {
                EndpointManager.getInstance().invoke("show_open_notification_dialog", this);
            }
        }

        chatIV = new RLottieImageView(this);
        contactsIV = new RLottieImageView(this);
//        workplaceIV = new RLottieImageView(this);
        meIV = new RLottieImageView(this);
        chatTV = new TextView(this);
        contactsTV = new TextView(this);
        meTV = new TextView(this);
        Typeface face = Typeface.createFromAsset(getResources().getAssets(),
                "fonts/mw_bold.ttf");
        chatTV.setTypeface(face);
        contactsTV.setTypeface(face);
        meTV.setTypeface(face);
        chatTV.setText(R.string.tab_text_chat);
        chatTV.setTextColor(ContextCompat.getColor(this, R.color.tab_text_normal));
        chatTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        contactsTV.setText(R.string.tab_text_contacts);
        contactsTV.setTextColor(ContextCompat.getColor(this, R.color.tab_text_normal));
        contactsTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        meTV.setText(R.string.tab_text_me);
        meTV.setTextColor(ContextCompat.getColor(this, R.color.tab_text_normal));
        meTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        List<Fragment> fragments = new ArrayList<>(3);
        fragments.add(new ChatFragment());
        fragments.add(new ContactsFragment());
        fragments.add(new MyFragment());

        msVBinding.vp.setAdapter(new MSFragmentStateAdapter(this, fragments));
        MSCommonModel.getInstance().getAppNewVersion(false, version -> {
            String v = MSDeviceUtils.getInstance().getVersionName(TabActivity.this);
            if (version != null && !TextUtils.isEmpty(version.download_url) && !version.app_version.equals(v)) {
                MSDialogUtils.getInstance().showNewVersionDialog(TabActivity.this, version);
            }
        });
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        MSCommonModel.getInstance().getAppConfig(null);
        msVBinding.bottomNavigation.getOrCreateBadge(R.id.i_chat).setVisible(false);
        msVBinding.bottomNavigation.getOrCreateBadge(R.id.i_my).setVisible(false);
        msVBinding.bottomNavigation.getOrCreateBadge(R.id.i_chat).setVisible(false);
        FrameLayout view = msVBinding.bottomNavigation.findViewById(R.id.i_chat);
        msgCounterView = new CounterView(this);
        msgCounterView.setColors(R.color.white, R.color.reminderColor);
        if (isShowTabText) {
            view.addView(chatIV, LayoutHelper.createFrame(35, 35, Gravity.CENTER | Gravity.TOP, 0, 5, 0, 0));
            view.addView(msgCounterView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 20, 5, 0, 15));
            view.addView(chatTV, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 15, 0, 0));
        } else {
            view.addView(chatIV, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
            view.addView(msgCounterView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 20, 5, 0, 15));
        }
        FrameLayout contactsView = msVBinding.bottomNavigation.findViewById(R.id.i_contacts);
        contactsCounterView = new CounterView(this);
        contactsCounterView.setColors(R.color.white, R.color.reminderColor);
        if (isShowTabText) {
            contactsView.addView(contactsIV, LayoutHelper.createFrame(35, 35, Gravity.CENTER | Gravity.TOP, 0, 5, 0, 0));
            contactsView.addView(contactsCounterView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 20, 5, 0, 15));
            contactsView.addView(contactsTV, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 15, 0, 0));
        } else {
            contactsView.addView(contactsIV, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
            contactsView.addView(contactsCounterView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 20, 5, 0, 15));
        }
        contactsSpotView = new View(this);
        contactsSpotView.setBackgroundResource(R.drawable.msg_bg);
        contactsView.addView(contactsSpotView, LayoutHelper.createFrame(10, 10, Gravity.CENTER_HORIZONTAL, 10, 10, 0, 0));
        FrameLayout meView = msVBinding.bottomNavigation.findViewById(R.id.i_my);
        if (isShowTabText) {
            meView.addView(meIV, LayoutHelper.createFrame(35, 35, Gravity.CENTER | Gravity.TOP, 0, 5, 0, 0));
            meView.addView(meTV, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 15, 0, 0));
        } else {
            meView.addView(meIV, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
        }
        contactsSpotView.setVisibility(View.GONE);
        contactsCounterView.setVisibility(View.GONE);
        msgCounterView.setVisibility(View.GONE);
        playAnimation(0);
    }

    @Override
    protected void initListener() {
        msVBinding.vp.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    playAnimation(0);
                    msVBinding.bottomNavigation.setSelectedItemId(R.id.i_chat);
                } else if (position == 1) {
                    playAnimation(1);
                    msVBinding.bottomNavigation.setSelectedItemId(R.id.i_contacts);
                } else {
                    playAnimation(3);
                    msVBinding.bottomNavigation.setSelectedItemId(R.id.i_my);
                }
            }
        });
        msVBinding.bottomNavigation.setItemIconTintList(null);
        msVBinding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.i_chat) {
                long nowTime = MSTimeUtils.getInstance().getCurrentMills();
                if (msVBinding.vp.getCurrentItem() == 0) {
                    if (nowTime - lastClickChatTabTime <= 300) {
                        EndpointManager.getInstance().invoke("scroll_to_unread_channel", null);
                    }
                    lastClickChatTabTime = nowTime;
                    return true;
                }
                msVBinding.vp.setCurrentItem(0);
                playAnimation(0);
            } else if (item.getItemId() == R.id.i_contacts) {
                msVBinding.vp.setCurrentItem(1);
                playAnimation(1);
            } else {
                msVBinding.vp.setCurrentItem(3);
                playAnimation(3);
            }
            return true;
        });
        EndpointManager.getInstance().setMethod("tab_activity", EndpointCategory.msRefreshMailList, object -> {
            getAllRedDot();
            return null;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllRedDot();
        boolean sync_friend = MSSharedPreferencesUtil.getInstance().getBoolean("sync_friend");
        if (sync_friend) {
            FriendModel.getInstance().syncFriends((code, msg) -> {
                if (code != HttpResponseCode.success && !TextUtils.isEmpty(msg)) {
                    showToast(msg);
                }
                if (code == HttpResponseCode.success) {
                    MSSharedPreferencesUtil.getInstance().putBoolean("sync_friend", false);
                }
            });
        }
    }

    public void setMsgCount(int number) {
        MSUIKitApplication.getInstance().totalMsgCount = number;
        if (number > 0) {
            msgCounterView.setCount(number, true);
            msgCounterView.setVisibility(View.VISIBLE);
        } else {
            msgCounterView.setCount(0, true);
            msgCounterView.setVisibility(View.GONE);
        }
    }

    public void setContactCount(int number, boolean showDot) {
        if (number > 0 || showDot) {
            if (number > 0) {
                contactsCounterView.setCount(number, true);
                contactsCounterView.setVisibility(View.VISIBLE);
                contactsSpotView.setVisibility(View.GONE);
            } else {
                contactsCounterView.setVisibility(View.GONE);
                contactsSpotView.setVisibility(View.VISIBLE);
                contactsCounterView.setCount(0, true);
            }
        } else {
            contactsCounterView.setVisibility(View.GONE);
            contactsSpotView.setVisibility(View.GONE);
        }
    }

    private void getAllRedDot() {
        boolean showDot = false;
        int totalCount = 0;
        int newFriendCount = MSSharedPreferencesUtil.getInstance().getInt(MSConfig.getInstance().getUid() + "_new_friend_count");
        totalCount = totalCount + newFriendCount;
        List<MailListDot> list = EndpointManager.getInstance().invokes(EndpointCategory.msGetMailListRedDot, null);
        if (MSReader.isNotEmpty(list)) {
            for (MailListDot MailListDot : list) {
                if (MailListDot != null) {
                    totalCount += MailListDot.numCount;
                    if (!showDot) showDot = MailListDot.showDot;
                }
            }
        }
        setContactCount(totalCount, showDot);
    }

    @Override
    public Resources getResources() {
        float fontScale = MSConstants.getFontScale();
        Resources res = super.getResources();
        Configuration config = res.getConfiguration();
        config.fontScale = fontScale; //1 设置正常字体大小的倍数
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    private void playAnimation(int index) {
        if (index == 0) {
            lastClickChatTabTime = 0;
            meIV.setImageResource(R.mipmap.ic_mine_n);
            contactsIV.setImageResource(R.mipmap.ic_contacts_n);
            chatIV.setImageResource(R.mipmap.ic_chat_s);
            if (isShowTabText) {
                chatTV.setTextColor(ContextCompat.getColor(this, R.color.tab_text_selected));
                contactsTV.setTextColor(ContextCompat.getColor(this, R.color.tab_text_normal));
                meTV.setTextColor(ContextCompat.getColor(this, R.color.tab_text_normal));
            }
        } else if (index == 1) {
            meIV.setImageResource(R.mipmap.ic_mine_n);
            chatIV.setImageResource(R.mipmap.ic_chat_n);
            contactsIV.setImageResource(R.mipmap.ic_contacts_s);
            if (isShowTabText) {
                chatTV.setTextColor(ContextCompat.getColor(this, R.color.tab_text_normal));
                contactsTV.setTextColor(ContextCompat.getColor(this, R.color.tab_text_selected));
                meTV.setTextColor(ContextCompat.getColor(this, R.color.tab_text_normal));
            }
        } else if (index == 2) {
            meIV.setImageResource(R.mipmap.ic_mine_n);
            chatIV.setImageResource(R.mipmap.ic_chat_n);
            contactsIV.setImageResource(R.mipmap.ic_contacts_n);
//            workplaceIV.setImageResource(R.mipmap.ic_contacts_s);
            if (isShowTabText) {
                chatTV.setTextColor(ContextCompat.getColor(this, R.color.tab_text_normal));
                contactsTV.setTextColor(ContextCompat.getColor(this, R.color.tab_text_normal));
                meTV.setTextColor(ContextCompat.getColor(this, R.color.tab_text_normal));
            }
        } else {
            chatIV.setImageResource(R.mipmap.ic_chat_n);
            contactsIV.setImageResource(R.mipmap.ic_contacts_n);
            meIV.setImageResource(R.mipmap.ic_mine_s);
//            workplaceIV.setImageResource(R.mipmap.ic_contacts_n);
            if (isShowTabText) {
                chatTV.setTextColor(ContextCompat.getColor(this, R.color.tab_text_normal));
                contactsTV.setTextColor(ContextCompat.getColor(this, R.color.tab_text_normal));
                meTV.setTextColor(ContextCompat.getColor(this, R.color.tab_text_selected));
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MSMultiLanguageUtil.getInstance().setConfiguration();
        Theme.applyTheme();
    }

    @Override
    public void finish() {
        super.finish();
        EndpointManager.getInstance().remove("tab_activity");
    }
}
