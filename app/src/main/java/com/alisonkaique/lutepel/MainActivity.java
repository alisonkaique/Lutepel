package com.alisonkaique.lutepel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.alisonkaique.lutepel.com.alisonkaique.lutepel.connector.SoapLogin;
import com.alisonkaique.lutepel.com.alisonkaique.lutepel.connector.SoapGetFil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private String developer_name = null;
    private EditText etUser = null;
    private EditText etPass = null;
    private boolean login_ctrl = false;

    //parametros a serem enviados para proxima pagina
    private String userCode = "";
    private String userName = "";
    private String userMail = "";

    //parametros Metodo Login
    String emp = "01"; //empresa
    String filial = "0101"; //filial

    //Itens do Spinner
    ArrayAdapter<String> adapter;
    private Spinner spFilial;
    private String[] spItens = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        etUser = (EditText) findViewById(R.id.etUser);
        etPass = (EditText) findViewById(R.id.etPassword);
        setSupportActionBar(toolbar);
        developer_name = getString(R.string.developer_name);

        //Pegando o objeto Spinner pelo ID
        spFilial = (Spinner) findViewById(R.id.spFilial);
        //Setando o adapter
        setAdapter();
        spFilial.setAdapter(adapter);
        //Definindo o Listener do Item
        spFilial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //pegando a filial
                filial = spItens[position];
                filial = filial.substring(0, 4);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Desenvolvido por: " + developer_name, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    //SetAdapter
    public void setAdapter(){
        String[] params = {emp};
        String resultado = "";
        try {
            resultado = new SoapGetFil().execute(params).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            resultado = "error: " + e.getMessage();
            resultado = resultado.toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
            resultado = "error: " + e.getMessage();
            resultado = resultado.toUpperCase();
        }

        Toast.makeText(MainActivity.this, resultado.substring(00, 05) + " " + Boolean.toString(resultado.substring(00, 05).equals("error")), Toast.LENGTH_LONG).show();

        //Caso de erro, mostrar mensagem ao usuario
        if (resultado.substring(00, 05).equals("error"))
        {
            Toast.makeText(MainActivity.this, resultado, Toast.LENGTH_LONG).show();
        } else { //Carrega Filiais
            try {
                //Desserializar a String JSON
                JSONObject jsonObject = new JSONObject(resultado);
                //verificar se nao houve erros
                Boolean is_error = jsonObject.has("error");

                if (is_error) {
                    Toast.makeText(MainActivity.this, "ERROR: " + jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                } else {
                    //Verificar se possui registros
                    int totalResults = jsonObject.getInt("totalResults");

                    if (totalResults > 0) {
                        JSONArray jsonArray = jsonObject.getJSONArray("Resources");

                        spItens = new String[jsonArray.length()];

                        //percorrendo o array
                        for (int i=0; i < jsonArray.length(); i++) {
                            try {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                //Adicionando valor do campo
                                spItens[i] = obj.getString("filCode") + "|" + obj.getString("filName");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "ERROR JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        //ArrayAdapter do Spinner
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spItens);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    //Sign In
    public void callSign(View view) {
        //Zerando erros
        etUser.setError(null);
        etPass.setError(null);

        login_ctrl = false;

        boolean cancel = false;
        View focusView = null;

        //Pegando a string do usuario e senha
        String user = etUser.getText().toString();
        String pass = etPass.getText().toString();

        //Validar se digitou o usuario
        if (TextUtils.isEmpty(user)) {
            etUser.setError(getString(R.string.error_invalid_user));
            focusView = etUser;
            cancel = true;
        } //Validar se digitou a senha
        else if (TextUtils.isEmpty(pass)) {
            etPass.setError(getString(R.string.error_invalid_pass));
            focusView = etPass;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            final ProgressDialog progDailog = ProgressDialog.show(this,
                    "Conectando aos servidores",
                    "Favor esperar...", true);
            new Thread() {
                public void run() {
                    try {
                        // sleep the thread, whatever time you want.
                        sleep(1000);
                    } catch (Exception e) {
                    }
                    progDailog.dismiss();
                }
            }.start();

            try {
                String[] params = {emp, filial, user, pass};
                String resultado = new SoapLogin().execute(params).get();

                try {
                    //Desserializar a String JSON
                    JSONObject jsonObject = new JSONObject(resultado);
                    //verificar se nao houve erros
                    Boolean is_error = jsonObject.has("error");

                    if (is_error) {
                        Toast.makeText(MainActivity.this, "ERROR: " + jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                    } else {
                        //alimentando parametros para proxima tela
                        userCode = jsonObject.getString("userCode");
                        userName = jsonObject.getString("userName");
                        userMail = jsonObject.getString("userMail");
                        login_ctrl = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    login_ctrl = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
                login_ctrl = false;
            }

            if (login_ctrl) {
                //Chamando Nova Activity
                Intent intent = new Intent(this, PainelGerencial.class);
                //passando informacoes pra intent
                intent.putExtra("filCode", filial);
                intent.putExtra("userCode", userCode);
                intent.putExtra("userName", userName);
                intent.putExtra("userMail", userMail);
                intent.putExtra("user", user);
                intent.putExtra("pass", pass);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
