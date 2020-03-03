package vitae.org.vitaewallet.ui.contacts_activity;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import vitae.org.vitaewallet.R;
import vitae.org.vitaewallet.ui.base.tools.adapter.BaseRecyclerViewHolder;

/**
 * Created by Neoperol on 5/18/17.
 */

public class ContactViewHolderBase extends BaseRecyclerViewHolder {

    CardView cv;
    TextView name;
    TextView address;

    ContactViewHolderBase(View itemView) {
        super(itemView);
        cv = (CardView) itemView.findViewById(R.id.cardView);
        name = (TextView) itemView.findViewById(R.id.name);
        address = (TextView) itemView.findViewById(R.id.address);
    }
}
