package co.yodo.launcher.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import co.yodo.launcher.R;
import co.yodo.launcher.ui.adapter.data.Currency;

/**
 * Created by luis on 16/12/14.
 * Adapter for the currency values
 */
public class CurrencyAdapter extends ArrayAdapter<Currency> {
    private static final int RESOURCE = R.layout.currency_row;
    private LayoutInflater inflater;

    static class ViewHolder {
        TextView nameTxVw;
    }

    public CurrencyAdapter( Context context, Currency[] objects ) {
        super( context, RESOURCE, objects );
        inflater = LayoutInflater.from( context );
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        ViewHolder holder;

        if( convertView == null ) {
            // inflate a new view and setup the view holder for future use
            convertView = inflater.inflate( RESOURCE, null );

            holder = new ViewHolder();
            holder.nameTxVw = (TextView) convertView.findViewById( R.id.currencyName );
            convertView.setTag( holder );
        } else {
            // view already defined, retrieve view holder
            holder = (ViewHolder) convertView.getTag();
        }

        Currency cat = getItem( position );

        holder.nameTxVw.setText( cat.getName() );
        holder.nameTxVw.setCompoundDrawables( cat.getImg(), null, null, null );

        return convertView;
    }
}
