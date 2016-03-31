package com.alisonkaique.lutepel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;

public class PainelGerencial extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnChartValueSelectedListener {

    private PieChart mChart;
    private String userCode = "";
    private String userName = "";
    private String userMail = "";
    private String user = "";
    private String pass = "";
    private String filial = "";
    private TextView txtUserName;
    private TextView txtUserMail;
    private EditText etErro;
    private String relName = "";
    private NumberFormat nf;

    private static final String NAMESPACE = "http://www.wststlutepel.com.br";
    private static String URL = "http://192.168.0.228:8093/ws/WSLUGRF.apw";
    private static final String METHOD_NAME = "GETGRF";
    private static final String SOAP_ACTION = "http://www.wststlutepel.com.br/GETGRF";

    private static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painel_gerencial);

        if (android.os.Build.VERSION.SDK_INT > 9) { // disabling strict-mode
            // for newer builds
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // Pega a formatacao do sistema, se for brasil R$ se EUA US$
        nf = NumberFormat.getCurrencyInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Pegando dados da outra activity
        Intent intent = getIntent();
        filial = intent.getStringExtra("filCode");
        userCode = intent.getStringExtra("userCode");
        userName = intent.getStringExtra("userName");
        userMail = intent.getStringExtra("userMail");
        user = intent.getStringExtra("user");
        pass = intent.getStringExtra("pass");

        //Atualizando Valores de Nome e E-mail
        View drawerHeader = navigationView.inflateHeaderView(R.layout.nav_header_painel_gerencial);
        txtUserName = (TextView) drawerHeader.findViewById(R.id.txtUserName);
        txtUserMail = (TextView) drawerHeader.findViewById(R.id.txtUserMail);
        txtUserName.setText(userName);
        txtUserMail.setText(userMail);

        //SOAP
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        //Propriedades //Parametros
        /*Filial*/
        /*Tipo do Gráfico*/
        PropertyInfo branch = new PropertyInfo();
        branch.setName("CFIL");
        branch.setValue(filial);
        branch.setType(branch.STRING_CLASS);
        request.addProperty(branch);
        /*Usuario*/
        PropertyInfo user = new PropertyInfo();
        user.setName("CUSER");
        user.setValue(userCode);
        user.setType(user.STRING_CLASS);
        request.addProperty(user);
        /*Tipo do Gráfico*/
        PropertyInfo graphType = new PropertyInfo();
        graphType.setName("CGRAPHTYPE");
        graphType.setValue("002");
        graphType.setType(graphType.STRING_CLASS);
        request.addProperty(graphType);
        /*Filtro do Gráfico*/
        PropertyInfo filter = new PropertyInfo();
        filter.setName("CFILTER");
        filter.setValue(0);
        filter.setType(filter.INTEGER_CLASS);
        request.addProperty(filter);

        SoapSerEnv envelope = new SoapSerEnv(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.implicitTypes = true;
        envelope.setAddAdornments(false);
        envelope.setOutputSoapObject(request);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

        try {
            androidHttpTransport.call(SOAP_ACTION, envelope);

            SoapPrimitive response = (SoapPrimitive)envelope.getResponse();
            //Toast.makeText(PainelGerencial.this, "SUCCESS: " + response.toString(), Toast.LENGTH_LONG).show();

            //Desserializar a String JSON
            JSONObject jsonObject = new JSONObject(response.toString());

            //Verificar se possui registros
            int totalResults = jsonObject.getInt("totalResults");
            //Pegando nome do relatório
            relName = jsonObject.getString("relName");

            //Definições do Grafico
            mChart = (PieChart) findViewById(R.id.chart1);
            mChart.setUsePercentValues(true);
            mChart.setDescription("Exemplo de Gráfico");
            mChart.setExtraOffsets(5, 10, 5, 5);

            mChart.setDragDecelerationFrictionCoef(0.95f);

            mChart.setCenterText(generateCenterSpannableText());

            mChart.setDrawHoleEnabled(true);
            mChart.setHoleColorTransparent(true);

            mChart.setTransparentCircleColor(Color.WHITE);
            mChart.setTransparentCircleAlpha(110);

            mChart.setHoleRadius(58f);
            mChart.setTransparentCircleRadius(61f);

            mChart.setDrawCenterText(true);

            mChart.setRotationAngle(0);
            // enable rotation of the chart by touch
            mChart.setRotationEnabled(true);
            mChart.setHighlightPerTapEnabled(true);

            // mChart.setUnit(" €");
            // mChart.setDrawUnitsInChart(true);

            // add a selection listener
            mChart.setOnChartValueSelectedListener(this);

            if (totalResults > 0) {
                JSONArray jsonArray = jsonObject.getJSONArray("Resources");

                //Setando informações para o gráfico
                //setData(04, 10);
                setInfo(jsonArray);
            }

            mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

            Legend l = mChart.getLegend();
            l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
            l.setXEntrySpace(7f);
            l.setYEntrySpace(0f);
            l.setYOffset(0f);

        } catch (IOException e1) {
            e1.printStackTrace();
            Toast.makeText(PainelGerencial.this, "ERROR IO: " + e1.getClass().getName() + " - " + e1.getMessage(), Toast.LENGTH_LONG).show();

        } catch (XmlPullParserException e1) {
            e1.printStackTrace();
            Toast.makeText(PainelGerencial.this, "ERROR XML: " + e1.getMessage(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(PainelGerencial.this, "ERROR EX: " + e.getClass().getName() + " - " + e.getMessage(), Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Funções complementares do gráfico
    private void setInfo(JSONArray jsonArray) {
        String[] xData = new String[jsonArray.length()];
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();

        //percorrendo o array
        for (int i=0; i < jsonArray.length(); i++) {
            try {
                JSONObject obj = jsonArray.getJSONObject(i);
                //Adicionando nome do campo
                xData[i] = obj.getString("field");
                //Adicionando valor do campo
                yVals1.add(new Entry((float) obj.getInt("value"), i));
                //Adicionando porcentagem do campo
                xVals.add(obj.getString("field"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        PieDataSet dataSet = new PieDataSet(yVals1, "Elementos");
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        //data.setValueTypeface(tf);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();

    }

    private SpannableString generateCenterSpannableText() {

        SpannableString s = new SpannableString(relName);
        return s;
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

        if (e == null)
            return;
        Log.i("VAL SELECTED",
                "Value: " + e.getVal() + ", xIndex: " + e.getXIndex()
                        + ", DataSet index: " + dataSetIndex);
        Toast.makeText(PainelGerencial.this, "Valor: " + nf.format(e.getVal()) /*e.getVal()*/ , Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected() {
        Log.i("PieChart", "nothing selected");
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }
}
