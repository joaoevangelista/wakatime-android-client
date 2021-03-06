package com.wakatime.android.user;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.wakatime.android.R;
import com.wakatime.android.WakatimeApplication;
import com.wakatime.android.dashboard.DashboardActivity;

import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.sephiroth.android.library.tooltip.Tooltip;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class UserStartActivity extends AppCompatActivity implements ViewModel {

    @BindView(R.id.edit_text_api_key)
    TextInputEditText mEditTextApiKey;

    @BindView(R.id.input_layout_api_key)
    TextInputLayout mInputLayoutApiKey;

    @BindView(R.id.loader_user)
    SpinKitView mLoaderUser;

    @BindView(R.id.welcome_text)
    TextView mWelcomeText;

    @BindView(R.id.container_user)
    RelativeLayout mContainerUser;

    @BindView(R.id.container_key)
    LinearLayout mContainerKey;

    @BindView(R.id.text_view_credits_icon)
    TextView mCreditsIcon;

    @BindView(R.id.text_view_credits_wakatime)
    TextView mCreditsWakatime;

    @Inject
    UserPresenter mPresenter;

    private Tracker mTracker;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_start);
        ButterKnife.bind(this);
        ((WakatimeApplication) this.getApplication()).useApiKeyComponent().inject(this);
        mTracker = ((WakatimeApplication) getApplication()).getTracker();
        mCreditsIcon.setMovementMethod(LinkMovementMethod.getInstance());
        mCreditsWakatime.setMovementMethod(LinkMovementMethod.getInstance());
        mPresenter.bind(this);
        mPresenter.checkIfKeyPresent();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("UserStart");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.unbind();
    }

    @Override
    public void sendUserToDashboard() {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Enter")
                .setAction("ValidKey").build());

        Intent dashboardIntent = new Intent(this, DashboardActivity.class);
        startActivity(dashboardIntent);
        finish(); // avoid user going back here
    }

    @Override
    public void setErrors(Map<String, Integer> errors) {
        if (!errors.isEmpty()) {
            mInputLayoutApiKey.setError(getString(errors.get("key_out_of_bounds")));

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Enter")
                    .setAction("InvalidKey").build());
        }
    }

    @Override
    public void showError() {
        Snackbar.make(mContainerUser,
                R.string.user_fetch_error,
                Snackbar.LENGTH_LONG);
    }

    @OnClick(R.id.btn_continue)
    public void onClick() {
        mPresenter.saveUserData(this.mEditTextApiKey.getText().toString());
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    @OnClick(R.id.btn_help)
    void onHelpClick(View button) {
        Tooltip.make(this, new Tooltip.Builder(101)
            .anchor(button, Tooltip.Gravity.LEFT)
            .closePolicy(new Tooltip.ClosePolicy()
                .insidePolicy(true, true)
                .outsidePolicy(true, false), 10000)
            .activateDelay(800)
            .showDelay(300)
            // beware this is using deprecated method by API 24
            // but lower API level don't have the new method
            .text(this.getResources(), R.string.api_key_help)
            .maxWidth(800)
            .withArrow(true)
            .withOverlay(true)
            .typeface(Typeface.createFromAsset(this.getAssets(), "fonts/Lato-Regular.ttf"))
            .withCallback(new Tooltip.Callback() {
                @Override
                public void onTooltipClose(Tooltip.TooltipView tooltipView, boolean fromUser, boolean containsTouch) {
                    if (fromUser && containsTouch) {
                        Uri wakatimeAddress = Uri.parse("https://wakatime.com/settings/account");
                        Intent webIntent = new Intent(Intent.ACTION_VIEW, wakatimeAddress);
                        startActivity(webIntent);
                    }
                }

                @Override
                public void onTooltipFailed(Tooltip.TooltipView tooltipView) {

                }

                @Override
                public void onTooltipShown(Tooltip.TooltipView tooltipView) {

                }

                @Override
                public void onTooltipHidden(Tooltip.TooltipView tooltipView) {

                }
            })
            .build()
        ).show();
    }

    @Override
    public void hideLoader() {
        this.mLoaderUser.setVisibility(View.GONE);
        this.mInputLayoutApiKey.setVisibility(View.VISIBLE);
        this.mWelcomeText.setVisibility(View.VISIBLE);
        this.mContainerKey.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLoader() {
        this.mLoaderUser.setVisibility(View.VISIBLE);
        this.mInputLayoutApiKey.setVisibility(View.GONE);
        this.mWelcomeText.setVisibility(View.GONE);
        this.mContainerKey.setVisibility(View.GONE);
    }
}
