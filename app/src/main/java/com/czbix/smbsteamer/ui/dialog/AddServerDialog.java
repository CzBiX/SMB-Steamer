package com.czbix.smbsteamer.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.czbix.smbsteamer.R;
import com.czbix.smbsteamer.dao.model.Credential;
import com.czbix.smbsteamer.dao.model.PasswordCredential;
import com.czbix.smbsteamer.dao.model.Server;
import com.google.common.base.Strings;

public class AddServerDialog extends DialogFragment implements DialogInterface.OnClickListener {
    private Listener mListener;
    private TextInputLayout mTextServer;
    private TextInputLayout mTextShare;
    private TextInputLayout mTextUsername;
    private TextInputLayout mTextPassword;
    private TextInputLayout mTextName;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = ((Listener) activity);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_add_server, null);
        mTextServer = ((TextInputLayout) view.findViewById(R.id.text_server));
        mTextShare = ((TextInputLayout) view.findViewById(R.id.text_share));
        mTextName = (TextInputLayout) view.findViewById(R.id.text_name);
        mTextUsername = ((TextInputLayout) view.findViewById(R.id.text_username));
        mTextPassword = ((TextInputLayout) view.findViewById(R.id.text_password));

        mTextShare.getEditText().addTextChangedListener(new TextWatcher() {
            private String lastStr = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                final EditText editText = mTextName.getEditText();
                if (TextUtils.equals(lastStr, editText.getText())) {
                    editText.setText(s);
                }
                lastStr = s.toString();
            }
        });
        mTextUsername.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updatePasswordStatus(s);
            }
        });
        updatePasswordStatus(null);

        builder.setTitle(R.string.title_add_server)
                .setView(view)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, this);

        return builder.create();
    }

    private void updatePasswordStatus(Editable s) {
        final boolean hasUsername = !TextUtils.isEmpty(s);
        mTextPassword.getEditText().setEnabled(hasUsername);
        mTextPassword.setHint(getString(hasUsername ? R.string.hint_password : R.string.hint_anonymous));
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                dialog.dismiss();
                Server server = buildServerInfo();
                mListener.onAdd(server);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.dismiss();
                mListener.onDismiss();
                break;
            default:
                throw new IllegalArgumentException("unknown button: " + which);
        }
    }

    private Server buildServerInfo() {
        final String server = mTextServer.getEditText().getText().toString();
        final String share = mTextShare.getEditText().getText().toString();
        final String name = Strings.emptyToNull(mTextName.getEditText().getText().toString());
        final Credential credential;
        if (TextUtils.isEmpty(mTextUsername.getEditText().getText())) {
            credential = Credential.ANONYMOUS;
        } else {
            final String username = mTextUsername.getEditText().getText().toString();
            final String password = mTextPassword.getEditText().getText().toString();
            credential = new PasswordCredential(username, password);
        }
        return new Server(server, share, name, credential);
    }

    public interface Listener {
        void onDismiss();
        void onAdd(Server server);
    }
}
