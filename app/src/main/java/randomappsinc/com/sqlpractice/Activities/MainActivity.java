package randomappsinc.com.sqlpractice.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.joanzapata.iconify.fonts.IoniconsIcons;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import randomappsinc.com.sqlpractice.Adapters.QuestionsAdapter;
import randomappsinc.com.sqlpractice.Database.MisterDataSource;
import randomappsinc.com.sqlpractice.Misc.Constants;
import randomappsinc.com.sqlpractice.Misc.PreferencesManager;
import randomappsinc.com.sqlpractice.Misc.TutorialServer;
import randomappsinc.com.sqlpractice.Misc.Utils;
import randomappsinc.com.sqlpractice.R;

public class MainActivity extends StandardActivity {
    @Bind(R.id.parent) View parent;
    @Bind(R.id.question_list) ListView questionList;

    private QuestionsAdapter questionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kill activity if it's not on top of the stack due to Samsung bug
        if (!isTaskRoot() && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                && getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_MAIN)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (PreferencesManager.get().getFirstTimeUser()) {
            new MaterialDialog.Builder(this)
                    .title(R.string.welcome)
                    .content(R.string.ask_for_help)
                    .positiveText(android.R.string.yes)
                    .show();
            PreferencesManager.get().setFirstTimeUser(false);
        }

        MisterDataSource m_dataSource = new MisterDataSource();
        m_dataSource.refreshTables();

        questionsAdapter = new QuestionsAdapter(this);
        questionList.setAdapter(questionsAdapter);

        if (PreferencesManager.get().shouldAskToRate()) {
            showPleaseRateDialog();
        }
    }

    private void showPleaseRateDialog() {
        new MaterialDialog.Builder(this)
                .content(R.string.please_rate)
                .negativeText(R.string.no_im_good)
                .positiveText(R.string.will_rate)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Uri uri =  Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        if (!(getPackageManager().queryIntentActivities(intent, 0).size() > 0)) {
                            Utils.showSnackbar(parent, getString(R.string.play_store_error));
                            return;
                        }
                        startActivity(intent);
                    }
                })
                .show();
    }

    @OnItemClick(R.id.question_list)
    public void onItemClick(int position) {
        Intent intent = new Intent(this, QuestionActivity.class);
        intent.putExtra(Constants.QUESTION_NUMBER_KEY, position);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        questionsAdapter.notifyDataSetChanged();
    }

    private void openWebPage(String helpURL) {
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra(WebActivity.IDEA_KEY, helpURL);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        Utils.loadMenuIcon(menu, R.id.library, IoniconsIcons.ion_ios_bookmarks);
        Utils.loadMenuIcon(menu, R.id.settings, IoniconsIcons.ion_android_settings);
        Utils.loadMenuIcon(menu, R.id.sandbox_mode, IoniconsIcons.ion_android_desktop);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.library:
                new MaterialDialog.Builder(this)
                        .title(R.string.library)
                        .items(TutorialServer.get().getLessonsArray())
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                openWebPage(text.toString());
                            }
                        })
                        .positiveText(R.string.close)
                        .show();
                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.sandbox_mode:
                startActivity(new Intent(this, SandboxActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}