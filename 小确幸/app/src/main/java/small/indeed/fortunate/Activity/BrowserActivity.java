package small.indeed.fortunate.Activity;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.net.*;
import android.net.http.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.text.*;
import android.text.method.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import java.util.*;
import small.indeed.fortunate.*;
import small.indeed.fortunate.Unit.*;
import small.indeed.fortunate.View.*;

import android.support.v7.app.AlertDialog;
import android.text.ClipboardManager;

public class BrowserActivity extends AppCompatActivity {
	
	private CoordinatorLayout coordinatorLayout;
	private SwipeRefreshLayout swipeRefreshLayout;
	private FloatingActionButton fab;
	private AgentWeb agentWeb;
	
	private View customView;
    private VideoView videoView;
    private int originalOrientation;
    private WebChromeClient.CustomViewCallback customViewCallback;
	private ValueCallback<Uri[]> filePathCallback = null;
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            filePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		init();
		initWebView();
	}
	
	@Override
    protected void onDestroy() {
        if (agentWeb != null) {
            agentWeb.clearHistory();
			agentWeb.clearCache(true);
			agentWeb.loadUrl("about:blank");

            ((ViewGroup) agentWeb.getParent()).removeView(agentWeb);
			agentWeb.removeAllViews();
			agentWeb.freeMemory();
			agentWeb.pauseTimers();
			agentWeb.destroy();
            agentWeb = null;
        }
        super.onDestroy();
    }
	
	@Override
	public void onBackPressed() {
		if (agentWeb.canGoBack()) {
			agentWeb.goBack();
		} else {
			super.onBackPressed();
		}
	}
	
	private void init() {
		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.rootView_browse);
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swip_refresh);
		agentWeb = (AgentWeb) findViewById(R.id.page);
		fab = (FloatingActionButton) findViewById(R.id.fab_menu);
		
		if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			if (getIntent().getData() != null) {
				this.agentWeb.loadUrl(getIntent().getData().toString());
			}
		} else if (Intent.ACTION_WEB_SEARCH.equals(getIntent().getAction())) {
			if (getIntent().getStringExtra("query").length() > 0) {
				this.agentWeb.loadUrl(BrowserUnit.SEARCH_ENGINE_BAIDU + getIntent().getStringExtra("query"));
			} else if (BrowserUnit.isURL(getIntent().getStringExtra("query"))) {
				this.agentWeb.loadUrl(getIntent().getStringExtra("query"));
			}
		} else {
			this.agentWeb.loadUrl(BrowserUnit.BASE_URL);
		}
		
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					agentWeb.reload();
				}
			});
		//swipeRefreshLayout.setDistanceToTriggerSync(ViewUnit.getWindowHeight(this)/3);
        swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
										  
		if (BrowserUnit.isURL(ClipboardUrl())) {
			Snackbar.make(coordinatorLayout, ClipboardUrl(), Snackbar.LENGTH_SHORT).setAction("打开", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						agentWeb.loadUrl(ClipboardUrl());
					}
				}).show();
		}
				
		fab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final MyBottomSheetDialog dialog = new MyBottomSheetDialog(BrowserActivity.this);
					View contentView = View.inflate(BrowserActivity.this, R.layout.layout_recycler, null);
					final RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.my_recycler_view);
					
					recyclerView.setItemAnimator(new DefaultItemAnimator());
					recyclerView.setLayoutManager(new LinearLayoutManager(BrowserActivity.this, LinearLayoutManager.VERTICAL, false));
					
					MyAdapter adapter = new MyAdapter(getData());
					recyclerView.setAdapter(adapter);
					
				

					dialog.setContentView(contentView);
					dialog.show();
				}
		});
		
	}
	
	private ArrayList<String> getData() {
        ArrayList<String> data = new ArrayList<>();
        String temp = " item";
        for(int i = 0; i < 100; i++) {
            data.add(i + temp);
        }

        return data;
    }
	
	private String ClipboardUrl() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService("clipboard");
        String str = "";
        if (clipboardManager != null) {
            try {
                if (clipboardManager.hasText()) {
                    CharSequence text = clipboardManager.getText();
                    clipboardManager.setText(text);
                    if (text != null && text.length() > 0) {
                        return text.toString().trim();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
        return str;
    }
	
	private void initWebView() {
		agentWeb.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					if (url.contains("baidu.com/wap/link") || url.contains("baidu.com/wap/shareview") || url.contains("baidu.com/s/") || url.contains("baidu.com/wap/view")) {
						agentWeb.getSettings().setUserAgentString(BrowserUnit.UA_SYMBIAN);
					} else {
						agentWeb.getSettings().setUserAgentString(agentWeb.getSettings().getUserAgentString());
					}
				}
				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					
				}
				@Override
				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
					super.onReceivedError(view, errorCode, description, failingUrl);
					ToastUtil.show(BrowserActivity.this, description);
				}
				@Override
				public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
					super.doUpdateVisitedHistory(view, url, isReload);
				}
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, final String url) {
					if (url.startsWith(WebView.SCHEME_TEL) || url.startsWith(WebView.SCHEME_GEO) || url.startsWith(WebView.SCHEME_MAILTO)) {
						try {
							Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
							startActivity(intent);
							return true;
						} catch (Exception e) {
							ToastUtil.show(BrowserActivity.this, R.string.toast_intent_failed);
						}
					}
					return super.shouldOverrideUrlLoading(view, url);
				}
				@Deprecated
				@Override
				public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
					return super.shouldInterceptRequest(view, url);
				}
				@Override
				public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
					return super.shouldInterceptRequest(view, request);
				}
				@Override
				public void onFormResubmission(WebView view, final Message dontResend, final Message resend) {
					AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
					builder.setCancelable(false);
					builder.setTitle(R.string.dialog_title_resubmission);
					builder.setMessage(R.string.dialog_content_resubmission);
					builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								resend.sendToTarget();
							}
						});
					builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dontResend.sendToTarget();
							}
						});
					builder.create().show();
				}
				@Override
				public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
					AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
					builder.setCancelable(false);
					builder.setTitle(R.string.dialog_title_warning);
					builder.setMessage(R.string.dialog_content_ssl_error);
					builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								handler.proceed();
							}
						});
					builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								handler.cancel();
							}
						});
					AlertDialog dialog = builder.create();
					if (error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
						dialog.show();
					} else {
						handler.proceed();
					}
				}
				@Override
				public void onReceivedHttpAuthRequest(WebView view, final HttpAuthHandler handler, String host, String realm) {
					AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
					builder.setCancelable(false);
					builder.setTitle(R.string.dialog_title_sign_in);
					LinearLayout signInLayout = (LinearLayout) LayoutInflater.from(BrowserActivity.this).inflate(R.layout.dialog_sign_in, null, false);
					final EditText userEdit = (EditText) signInLayout.findViewById(R.id.dialog_sign_in_username);
					final EditText passEdit = (EditText) signInLayout.findViewById(R.id.dialog_sign_in_password);
					passEdit.setTypeface(Typeface.DEFAULT);
					passEdit.setTransformationMethod(new PasswordTransformationMethod());
					builder.setView(signInLayout);
					builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								String user = userEdit.getText().toString().trim();
								String pass = passEdit.getText().toString().trim();
								handler.proceed(user, pass);
							}
						});
					builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								handler.cancel();
							}
						});
					builder.create().show();
				}
			});

		agentWeb.setWebChromeClient(new WebChromeClient() {
				@Override
				public void onProgressChanged(WebView view, int progress) {
					super.onProgressChanged(view, progress);
					if (progress == 100) {
						swipeRefreshLayout.setRefreshing(false);
					} else if (!swipeRefreshLayout.isRefreshing()) {
						swipeRefreshLayout.setRefreshing(true);
					}
				}
				@Override
				public void onReceivedTitle(WebView view, String title) {
					super.onReceivedTitle(view, title);
					//inputBox.setText(title.toString().length() == 0 ? view.getUrl() : title);
				}
				@Override
				public void onReceivedIcon(WebView view, Bitmap icon) {
					super.onReceivedIcon(view, icon);
				}//imageView.setImageBitmap(icon);
				@Deprecated
				@Override
				public void onShowCustomView(View view, int requestedOrientation, WebChromeClient.CustomViewCallback callback) {
					super.onShowCustomView(view, requestedOrientation, callback);
					onShowCustomView(view, requestedOrientation, callback);
				}
				@Override
				public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
					super.onShowCustomView(view, callback);
					if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					}
				}
				@Override
				public void onHideCustomView() {
					super.onHideCustomView();
					if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					}
				}
				@Override
				public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
					super.onGeolocationPermissionsShowPrompt(origin, callback);
					AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
					builder.setCancelable(false);
					builder.setMessage("允许网页访问地理位置");
					builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								callback.invoke(origin, true, true);
							}
						});
					builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								callback.invoke(origin, false, true);
							}
						});
					builder.create().show();
				}
				//设置响应js 的Alert()函数
				@Override
				public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
					AlertDialog.Builder b = new AlertDialog.Builder(BrowserActivity.this);
					b.setMessage(message);
					b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								result.confirm();
							}
						});
					b.setCancelable(false);
					b.create().show();
					return true;
				}
				//设置响应js 的Confirm()函数
				@Override
				public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
					AlertDialog.Builder b = new AlertDialog.Builder(BrowserActivity.this);
					b.setMessage(message);
					b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								result.confirm();
							}
						});
					b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								result.cancel();
							}
						});
					b.setCancelable(false);
					b.create().show();
					return true;
				}
				//设置响应js 的Prompt()函数
				@Override
				public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
					View v =  LayoutInflater.from(BrowserActivity.this).inflate(R.layout.dialog_edit, null);
					final EditText editText = (EditText) v.findViewById(R.id.layout_dialog_edit_text);
					editText.setText(defaultValue);
					AlertDialog.Builder b = new AlertDialog.Builder(BrowserActivity.this);
					b.setMessage(message);
					b.setView(v);
					b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								String value = editText.getText().toString().trim();
								result.confirm(value);
							}
						});
					b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								result.cancel();
							}
						});
					b.setCancelable(false);
					b.create().show();
					return true;
				}
				// For Android 5.0+
				@Override
				public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> valueCallback, WebChromeClient.FileChooserParams fileChooserParams) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						filePathCallback = valueCallback;
						try {
							Intent intent = fileChooserParams.createIntent();
							startActivityForResult(intent, IntentUnit.REQUEST_FILE_21);
						} catch (Exception e) {
							ToastUtil.show(BrowserActivity.this, R.string.toast_open_file_manager_failed);
						}
					}
					return true;
				}
			});

		agentWeb.setDownloadListener(new DownloadListener() {
				@Override
				public void onDownloadStart(final String url, String userAgent, final String contentDisposition, final String mimeType, long contentLength) {
					if (BrowserUnit.hasApp(BrowserActivity.this, "com.dv.adm.pay")) {
						BrowserUnit.downloadByADM(BrowserActivity.this, url, mimeType);
					} else {
						Snackbar.make(coordinatorLayout, contentDisposition, Snackbar.LENGTH_INDEFINITE).setAction("下载", new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									BrowserUnit.download(BrowserActivity.this, url, contentDisposition, mimeType);
								}
							}).show();
					}
				}
			});

		agentWeb.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					final WebView.HitTestResult result = agentWeb.getHitTestResult();
					final String target = result.getExtra();

					final ArrayList<String> list = new ArrayList<String>();
					list.add(0, getString(R.string.dialog_longpress_new_tab));
					list.add(1, getString(R.string.dialog_longpress_new_tab_in_background));
					list.add(2, getString(R.string.dialog_longpress_copy_link));
					if (result != null && (result.getType() == WebView.HitTestResult.IMAGE_TYPE || result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE)) {
						list.add(3, getString(R.string.dialog_longpress_save_img));
					}
					
					AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
					builder.setItems(list.toArray(new String[list.size()]), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
									case 0:
										Intent intent = new Intent(BrowserActivity.this, BrowserActivity.class);
										intent.setData(Uri.parse(target));
										intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
										startActivity(intent, ActivityOptions.makeBasic().toBundle());
										break;
									case 1:
										Intent intent2 = new Intent(BrowserActivity.this, BrowserActivity.class);
										intent2.setData(Uri.parse(target));
										intent2.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
										startActivity(intent2, ActivityOptions.makeTaskLaunchBehind().toBundle());
										break;
									case 2:
										BrowserUnit.copyURL(BrowserActivity.this, target);
										break;
									case 3:
										if (BrowserUnit.hasApp(BrowserActivity.this, "com.dv.adm.pay")) {
											BrowserUnit.downloadByADM(BrowserActivity.this, target, BrowserUnit.MIME_TYPE_IMAGE);
										} else {
											Snackbar.make(coordinatorLayout, target, Snackbar.LENGTH_INDEFINITE).setAction("下载", new View.OnClickListener() {
													@Override
													public void onClick(View v) {
														BrowserUnit.download(BrowserActivity.this, target, target, BrowserUnit.MIME_TYPE_IMAGE);
													}
												}).show();
										}
										break;
									default:
										break;
								}
							}
						});

					if (target != null || (result != null && result.getExtra() != null)) {
						if (result == null) {
							result.getExtra();
						}
						AlertDialog alertDialog = builder.create();
						alertDialog.show();
					}

					return false;
				}
			});
			
		agentWeb.setOnTouchListener(new View.OnTouchListener() {
				float location = 0;
				float y = 0;
				int action = 0;
				@Override
				public boolean onTouch(View view, MotionEvent arg1) {
					if (view != null && !view.hasFocus()) {
						view.requestFocus();
					}
					action = arg1.getAction();
					y = arg1.getY();
					if (action == MotionEvent.ACTION_DOWN) {
						location = y;
					} else if (action == MotionEvent.ACTION_UP) {
						if ((y - location) > 20) {
							if (agentWeb.getScrollY() < 5 && fab.isShown()) {
								fab.hide();
							} else {
								fab.show();
							}
						} else if ((y - location) < -20) {
							fab.hide();
						}
						location = 0;
					}

					return false;
				}

			});
			
	}
	
	public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

		private ArrayList<String> mData;

		public MyAdapter(ArrayList<String> data) {
			this.mData = data;
		}

		public void updateData(ArrayList<String> data) {
			this.mData = data;
			notifyDataSetChanged();
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			// 实例化展示的view
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item, parent, false);
			// 实例化viewholder
			ViewHolder viewHolder = new ViewHolder(v);
			return viewHolder;
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			// 绑定数据
			holder.mTv.setText(mData.get(position));
		}

		@Override
		public int getItemCount() {
			return mData == null ? 0 : mData.size();
		}

		public class ViewHolder extends RecyclerView.ViewHolder {

			TextView mTv;

			public ViewHolder(View itemView) {
				super(itemView);
				mTv = (TextView) itemView.findViewById(R.id.item_tv);
			}
		}
	}
	
}
