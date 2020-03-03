package vitae.org.vitaewallet.ui.transaction_send_activity.custom.inputs;

import org.sendj.core.Sha256Hash;
import org.sendj.core.TransactionOutput;

import java.io.Serializable;

import vitae.org.vitaewallet.contacts.AddressLabel;
import vitae.org.vitaewallet.module.VitaeContext;

/**
 * Created by kaali on 8/4/17.
 */

public class InputWrapper implements Serializable{

    private transient TransactionOutput unspent;
    private Sha256Hash parentTxHash;
    private int index;
    private AddressLabel addressLabel;

    public InputWrapper(TransactionOutput unspent, AddressLabel addressLabel) {
        this.unspent = unspent;
        this.addressLabel = addressLabel;
        if (unspent!=null) {
            parentTxHash = unspent.getParentTransactionHash();
            index = unspent.getIndex();
        }
    }

    public TransactionOutput getUnspent() {
        return unspent;
    }

    public void setUnspent(TransactionOutput unspent) {
        this.unspent = unspent;
    }

    public AddressLabel getAddressLabel() {
        return addressLabel;
    }

    public Sha256Hash getParentTxHash() {
        return parentTxHash;
    }

    public int getIndex() {
        return index;
    }


    public String getLabel() {
        return addressLabel !=null? addressLabel.toLabel():unspent.getScriptPubKey().getToAddress(VitaeContext.NETWORK_PARAMETERS,true).toBase58();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InputWrapper that = (InputWrapper) o;

        if (index != that.index) return false;
        return parentTxHash.equals(that.parentTxHash);

    }

    @Override
    public int hashCode() {
        int result = parentTxHash.hashCode();
        result = 31 * result + index;
        return result;
    }
}
