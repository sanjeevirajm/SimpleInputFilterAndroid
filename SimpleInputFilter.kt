import android.text.InputFilter
import android.text.Spanned
import com.zoho.people.utils.log.Logger.log
import com.zoho.people.utils.log.Logger.logWithTag
import com.zoho.people.utils.log.Logger.runIfLogsEnabled

/**
 Why SimpleInputFilter?
 * It needs a lot of time to deeply understand Input filter
 * Even if we understood it completely, it takes considerable amount of time to fix a bug after some weeks
 * To avoid checking whole text instead of entered text
 */

abstract class SimpleInputFilter : InputFilter {
    var isTextWasPastedOrSetProgrammatically: Boolean = false

    final override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        logWithTag {
            "SimpleInputFilter source: $source"
        }

        if (source == null) {
            return null
        }

        val enteredText = source.subSequence(start, end)

//        val fullText = SpannableStringBuilder()
        val textBeforeEnteredText: CharSequence = dest?.subSequence(0, dstart) ?: ""
        val textAfterEnteredText: CharSequence = dest?.subSequence(dend, dest.length) ?: ""

        val fullText = StringBuilder(textBeforeEnteredText.length + enteredText.length + textAfterEnteredText.length)

        fullText.append(textBeforeEnteredText)
        fullText.append(enteredText)
        fullText.append(textAfterEnteredText)

        isTextWasPastedOrSetProgrammatically = textBeforeEnteredText.isEmpty() && textAfterEnteredText.isEmpty() && enteredText.length > 1

        runIfLogsEnabled {
            log = "SimpleInputFilter, textBeforeEnteredText: $textBeforeEnteredText"
            log = "SimpleInputFilter, enteredText: $enteredText"
            log = "SimpleInputFilter, textAfterEnteredText: $textAfterEnteredText"
            log = "SimpleInputFilter, isTextWasPastedOrSetProgrammatically: $isTextWasPastedOrSetProgrammatically"
        }

        val modifiedEnteredText = filterText(
            fullText,
            textBeforeEnteredText,
            textAfterEnteredText,
            enteredText
        )

        logWithTag {
            "SimpleInputFilter, modifiedEnteredText: $modifiedEnteredText"
        }

        return modifiedEnteredText
    }

    /**
     Don't care about spannable
     You must have noticed TextUtils.copySpansFrom in android documentation and stackoverflow answers.
     Leave it. It's hard to copy spans to the exact position in  filtered text, it's hard to test,
     need to write two implementation, one for string and one for spannable (overkill for most of the apps)
     It is needed only if you are supporting formatted text like bold, italic, etc..
     if you are supporting such format, you can't use this class. go ahead with default InputFilter
     */

    // return modified text. If unmodified, return null (android documentation says that. not sure why. maybe it is efficient)
    abstract fun filterText(
        fullText: CharSequence,
        textBeforeEnteredText: CharSequence,
        textAfterEnteredText: CharSequence,
        enteredText: CharSequence
    ): CharSequence?
}
