package small.indeed.fortunate.View;

import android.content.*;
import android.util.*;
import android.webkit.*;
import android.view.*;

public class AgentWeb extends WebView {

	public AgentWeb(Context context) {
        super(context);
		this.initWebView();
		this.initWebSettings();
    }

    public AgentWeb(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
		this.initWebView();
		this.initWebSettings();
    }

    void initWebView() {
        setDrawingCacheEnabled(true);
        setDrawingCacheBackgroundColor(0);
        setDrawingCacheQuality(0);
        setWillNotCacheDrawing(false);
        setSaveEnabled(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setScrollbarFadingEnabled(true);
        setOverScrollMode(0);
        setWebViewClient(null);
        setWebChromeClient(null);
        setDownloadListener(null);
        setOnTouchListener(null);
    }

    void initWebSettings() {
        getSettings().setAllowContentAccess(true);
        getSettings().setAllowFileAccess(true);
        getSettings().setAllowFileAccessFromFileURLs(true);
        getSettings().setAllowUniversalAccessFromFileURLs(true);
        getSettings().setAppCacheEnabled(true);
        getSettings().setAppCachePath(getContext().getCacheDir().toString());
        getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        getSettings().setDatabaseEnabled(true);
        getSettings().setDomStorageEnabled(true);
        getSettings().setSaveFormData(true);
        getSettings().setSupportZoom(true);
        getSettings().setBuiltInZoomControls(true);
        getSettings().setDisplayZoomControls(false);
        getSettings().setTextZoom(100);
        getSettings().setUseWideViewPort(true);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        getSettings().setMediaPlaybackRequiresUserGesture(true);
        getSettings().setBlockNetworkImage(false);
        getSettings().setBlockNetworkLoads(false);
        getSettings().setGeolocationEnabled(true);
		getSettings().setGeolocationDatabasePath(getContext().getFilesDir().toString());
        getSettings().setSupportMultipleWindows(true);
        getSettings().setDefaultTextEncodingName("UTF-8");
		getSettings().setPluginState(WebSettings.PluginState.ON);
        getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        getSettings().setLoadWithOverviewMode(true);
        getSettings().setLoadsImagesAutomatically(true);
        getSettings().setMixedContentMode(0);
        getSettings().setUserAgentString(getSettings().getUserAgentString());
    }
	
	public static void destroyWebView(WebView view) {
		if (view != null) {
            view.clearHistory();
			view.clearCache(true);
			view.clearSslPreferences();
			view.setWebViewClient(null);
			view.setWebChromeClient(null);
			view.loadDataWithBaseURL(null, "", "text/html", "UTF-8", null);

            ((ViewGroup) view.getParent()).removeView(view);
			view.removeAllViews();
			view.freeMemory();
			view.pauseTimers();
			view.destroy();
            view = null;
        }
    }
	

	public void pause() {
        onPause();
        pauseTimers();
    }

    public void resume() {
        onResume();
        resumeTimers();
    }

	@Override
	public void destroy() {
        stopLoading();
        onPause();
        clearHistory();
        removeAllViews();
        destroyDrawingCache();
        super.destroy();
    }
}



