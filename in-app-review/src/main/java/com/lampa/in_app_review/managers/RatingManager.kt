package com.lampa.in_app_review.managers

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory
import com.lampa.in_app_review.R

public class RatingManager(
    private val appContext: Context,
    private val sharPref: SharePreferencesManager,
    private val feedbackEmail: String
) {
    companion object {
        private val TAG_RATING = this::class.java.simpleName
    }

    private lateinit var reviewString: String
    private lateinit var yesString: String
    private lateinit var noString: String
    private lateinit var laterString: String
    private lateinit var cancelString: String
    private lateinit var leaveFeedbackString: String

    private lateinit var doYouLikeAppString: String
    private lateinit var whatDoYouDislikeString: String
    private lateinit var wouldYouLikeToRateString: String

    fun setupRatingFlags() {
        if (!sharPref.isRatingFlowFinished) {
            val openedAppTimes = sharPref.openedAppTimes

            Log.d("TAG_RATING", "App: openedAppTimes: $openedAppTimes")
            if (openedAppTimes < 2) sharPref.openedAppTimes = openedAppTimes + 1

            Log.d("TAG_RATING", "App: openedAppTimes: ${sharPref.openedAppTimes}")

            sharPref.shouldStartRatingFlow = true
            Log.d(TAG_RATING, "shouldStartRatingFlow: ${sharPref.shouldStartRatingFlow}")
        }
    }

    fun startRatingFlow(activity: Activity) {
        if (
            !sharPref.isRatingFlowFinished &&
            sharPref.shouldStartRatingFlow &&
            sharPref.openedAppTimes > 1
        ) {
            sharPref.shouldStartRatingFlow = false

            activity.apply {
                reviewString = getString(R.string.review)
                yesString = getString(R.string.yes)
                noString = getString(R.string.no)
                laterString = getString(R.string.later)
                cancelString = getString(R.string.cancel)
                leaveFeedbackString = getString(R.string.leave_feedback)

                doYouLikeAppString = getString(R.string.do_you_like_app)
                whatDoYouDislikeString = getString(R.string.what_do_you_dislike)
                wouldYouLikeToRateString = getString(R.string.would_you_like_to_rate)
            }

            showMainDialog(activity)
        }
    }

    private fun requestReviewFlow(activity: Activity, onFinish: () -> Unit) {
        val manager = ReviewManagerFactory.create(appContext)
        // val manager = FakeReviewManager(appContext) // Fake for testing purposes

        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(activity, reviewInfo!!)
                flow.addOnCompleteListener { onFinish.invoke() }

            } else {
                Log.e(TAG_RATING, "Rating task isn't successful: ${task.exception?.message}")
                val appPackageName: String = activity.packageName // package name of the app

                try {
                    activity.startActivity(Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$appPackageName"
                    )))
                } catch (exception: ActivityNotFoundException) {
                    activity.startActivity(Intent(
                        Intent.ACTION_VIEW, Uri.parse(
                "https://play.google.com/store/apps/details?id=$appPackageName"
                    )))
                }
            }
        }
    }

    private fun showMainDialog(activity: Activity) {
        val builder = AlertDialog.Builder(activity, R.style.ReviewDialog).apply {
            setTitle(reviewString)
            setMessage(doYouLikeAppString)
            setPositiveButton(yesString) { _, _ -> showRatingDialog(activity) }
            setNegativeButton(noString) { _, _ ->
                completeFlow()
                showFeedbackDialog(activity)
            }
            setNeutralButton(laterString, null)
        }

        builder.create().show()
    }

    private fun showFeedbackDialog(activity: Activity) {
        val builder = AlertDialog.Builder(activity, R.style.ReviewDialog).apply {
            setTitle(reviewString)
            setMessage(whatDoYouDislikeString)
            setPositiveButton(leaveFeedbackString) { _, _ ->
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    val subject = activity.getString(R.string.review_email_subject)

                    data = Uri.parse("mailto:$feedbackEmail?subject=${Uri.encode(subject)}")

                    putExtra(Intent.EXTRA_EMAIL, feedbackEmail)
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                }

                if (intent.resolveActivity(context.packageManager) != null)
                    context.startActivity(intent)
            }
            setNegativeButton(cancelString, null)
        }
        builder.create().show()
    }

    private fun showRatingDialog(activity: Activity) {
        val builder = AlertDialog.Builder(activity, R.style.ReviewDialog).apply {
            setTitle(activity.getString(R.string.review))
            setMessage(wouldYouLikeToRateString)
            setPositiveButton(yesString) { _, _ ->
                completeFlow()
                requestReviewFlow(activity) { }
            }
            setNeutralButton(laterString, null)
            setNegativeButton(cancelString) { _, _ -> completeFlow() }
        }

        builder.create().show()
    }

    private fun completeFlow() {
        sharPref.isRatingFlowFinished = true
    }


}