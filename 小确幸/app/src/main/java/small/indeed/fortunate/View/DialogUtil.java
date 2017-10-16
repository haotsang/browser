package small.indeed.fortunate.View;

import android.app.*;
import android.content.*;
import android.view.*;
import android.view.ViewGroup.*;
import android.widget.*;
import small.indeed.fortunate.*;
import small.indeed.fortunate.Unit.*;
import android.os.*;
import android.text.*;
import java.util.*;

public class DialogUtil
{
	private AlertDialog alertDialog;
	
    private SureInterfance sureListener;
    private ICancelListener cancelListener;
	
    /**
     * @param context
     * @param listener  左边按钮确定监听
     * @param iCancelListener 右边按钮确定监听
     */
	 
    public DialogUtil(final Context context, SureInterfance listener, ICancelListener iCancelListener)
	{
		final View layout = LayoutInflater.from(context).inflate(R.layout.dialog_search, null);
		final AutoCompleteTextView inputBox = (AutoCompleteTextView) layout.findViewById(R.id.layout_dialog_search_box);
		final ImageButton inputDelete = (ImageButton) layout.findViewById(R.id.layout_dialog_search_delete);
		
		alertDialog = new AlertDialog.Builder(context).setView(layout).create();
		//alertDialog.setMessage(inputBox.getText().toString());
		alertDialog.setCancelable(false);
		
        sureListener = listener;
        cancelListener = iCancelListener;
		
        inputDelete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (inputBox.getText().toString().isEmpty()) {
						BrowserUnit.hideSoftInput(context, inputBox);
						alertDialog.dismiss();
					} else {
						inputBox.setText(null);
					}
				}
			});
			
		inputBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
				{
					String query = inputBox.getText().toString().trim();
					if (query.isEmpty()) { return true; }
					
					sureListener.sureTodo(query);
					BrowserUnit.hideSoftInput(context, inputBox);
					alertDialog.dismiss();

					return false;
				}
			});
			BrowserUnit.showSoftInput(context, inputBox);
    }
	
	
	
    public interface SureInterfance {
        void sureTodo(String query);
    }
	
    public interface ICancelListener {
        void cancelTodo();
    }
	
    public void showDialog() {
		alertDialog.getWindow().setGravity(Gravity.CENTER | Gravity.TOP);
		//alertDialog.getWindow().setWindowAnimations(R.style.dialogAnimation);
        alertDialog.show();
    }
    
}

