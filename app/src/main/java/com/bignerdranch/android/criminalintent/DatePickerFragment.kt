package com.bignerdranch.android.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import java.util.*

private const val ARG_DATE = "date"
private const val ARG_REQUEST_CODE = "requestCode"

class DatePickerFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


        val date = arguments?.getSerializable(ARG_DATE) as Date


        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val resultDate : Date = GregorianCalendar(year, month, dayOfMonth).time
            // 仅在监听其中用到，故在内部初始化
            val requestKey = arguments?.getSerializable(ARG_REQUEST_CODE) as String

            setFragmentResult(requestKey, bundleOf(ARG_DATE to resultDate))
        }


        val calendar = Calendar.getInstance()
        calendar.time = date
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDay
        )
    }


    companion object {
        fun newInstance(date: Date,requestCode: String): DatePickerFragment {
            // 初始化一个Bundle用于塞入对象中
            // date由外部传入，用于初始化当前对象的date
            val args = Bundle().apply {
                putSerializable(ARG_DATE,date)
                putSerializable(ARG_REQUEST_CODE,requestCode)
            }

            return DatePickerFragment().apply {
                arguments = args
                // Fragment能拿到请求，我们如何监听 新的结果？
            }
        }
    }
}