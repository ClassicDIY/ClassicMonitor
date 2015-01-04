/*
 * Copyright (c) 2014. FarrelltonSolar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ca.farrelltonsolar.classic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;

import java.util.regex.Pattern;

public class IPAddressDialog extends DialogFragment {
    static private final String IPV4_REGEX = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
    static private Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);
    private static Gson GSON = new Gson();

    public static IPAddressDialog newInstance(int title) {
        IPAddressDialog frag = new IPAddressDialog();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.dialog_static_address, null);
        builder.setView(view);
        EditText edAddress = (EditText) view.findViewById(R.id.ipAddress);
        edAddress.setKeyListener(IPAddressKeyListener.getInstance());

        builder.setPositiveButton(R.string.ApplyButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String port = ((EditText) getDialog().findViewById(R.id.port)).getText().toString();
                String edAddress = ((EditText) getDialog().findViewById(R.id.ipAddress)).getText().toString();
                if (!port.isEmpty() && !edAddress.isEmpty()) {
                    if (IPV4_PATTERN.matcher(edAddress).matches()) {
                        ChargeControllerInfo cc = new ChargeControllerInfo(edAddress, Integer.valueOf(port), true);
                        cc.setIsReachable(false);
                        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(IPAddressDialog.this.getActivity());
                        Intent pkg = new Intent(Constants.CA_FARRELLTONSOLAR_CLASSIC_ADD_CHARGE_CONTROLLER);
                        pkg.putExtra("ChargeController", GSON.toJson(cc));
                        pkg.putExtra("ForceRefresh", true);
                        broadcaster.sendBroadcast(pkg);
                        dialog.dismiss();
                    }
                }
            }
        }).setNegativeButton(R.string.CancelButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnIPAddressDialogInteractionListener {
        public void onAddChargeController(ChargeControllerInfo cc);
    }

}
