package small.indeed.fortunate.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import small.indeed.fortunate.R;
import android.support.compat.*;

public class FinderBar extends FrameLayout {
    private EditText etKeyWords;
    private AppCompatImageButton imbCloseFinder;
    private AppCompatImageButton imbFindNext;
    private AppCompatImageButton imbFindPre;
    private FindTextChangedListener mFindTextChangedListener;
    private FinderListener mFinderListener;
    private TextView tvMatchesNumber;

    public interface FinderListener {
        void onClosed();

        void onFindKeyChanged(CharSequence charSequence);

        void onNextClick();

        void onPreClick();
    }

    private class FindTextChangedListener implements TextWatcher {
        private FindTextChangedListener() {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (FinderBar.this.mFinderListener != null) {
                FinderBar.this.mFinderListener.onFindKeyChanged(s);
            }
        }

        public void afterTextChanged(Editable s) {
        }
    }

    public EditText getEtKeyWords() {
        return this.etKeyWords;
    }

    public void setFinderListener(@Nullable FinderListener finderListener) {
        this.mFinderListener = finderListener;
    }

    public FinderBar(@NonNull Context context) {
        this(context, null);
    }

    public FinderBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FinderBar(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.finder_bar, this, true);
        setBackgroundColor(-1);
        this.imbFindNext = (AppCompatImageButton) findViewById(R.id.imb_next_finder);
        this.imbFindPre = (AppCompatImageButton) findViewById(R.id.imb_previous_finder);
        this.imbCloseFinder = (AppCompatImageButton) findViewById(R.id.imb_close_finder);
        this.tvMatchesNumber = (TextView) findViewById(R.id.tv_matches_number_finder);
        this.etKeyWords = (EditText) findViewById(R.id.et_key_finder);
     //   TintUtils.setImageButtonTint(this.imbCloseFinder, Color.parseColor("#757575"));
     //   ViewCompat.setElevation(this, (float) SizeUtils.dp2px(context, 16.0f));
        setListener();
    }

    public void setMatchesNumber(int activeMatch, int numberOfMatches) {
        if (numberOfMatches == 0) {
            this.tvMatchesNumber.setText("0/0");
        } else {
            this.tvMatchesNumber.setText(activeMatch + "/" + numberOfMatches);
        }
    }

    public void closeFinderBar() {
        setVisibility(8);
        this.etKeyWords.setText(BuildConfig.VERSION_NAME);
        this.mFinderListener.onClosed();
    }

    private void setListener() {
        this.tvMatchesNumber.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
				}
			});
        this.imbCloseFinder.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					FinderBar.this.closeFinderBar();
				}
			});
        this.imbFindNext.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (FinderBar.this.mFinderListener != null) {
						FinderBar.this.mFinderListener.onNextClick();
					}
				}
			});
        this.imbFindPre.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (FinderBar.this.mFinderListener != null) {
						FinderBar.this.mFinderListener.onPreClick();
					}
				}
			});
        this.mFindTextChangedListener = new FindTextChangedListener();
        this.etKeyWords.addTextChangedListener(this.mFindTextChangedListener);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.etKeyWords.removeTextChangedListener(this.mFindTextChangedListener);
    }
}

