package com.bn.easyscanner.extensions

import android.content.Intent
import androidx.core.content.IntentCompat
import com.bn.easyscanner.content.QRContent
import com.google.mlkit.vision.barcode.common.Barcode
import com.bn.easyscanner.QRScannerActivity
import com.bn.easyscanner.QRScannerActivity.Companion.EXTRA_RESULT_BYTES
import com.bn.easyscanner.QRScannerActivity.Companion.EXTRA_RESULT_EXCEPTION
import com.bn.easyscanner.QRScannerActivity.Companion.EXTRA_RESULT_PARCELABLE
import com.bn.easyscanner.QRScannerActivity.Companion.EXTRA_RESULT_VALUE
import com.bn.easyscanner.content.AddressParcelable
import com.bn.easyscanner.content.CalendarDateTimeParcelable
import com.bn.easyscanner.content.CalendarEventParcelable
import com.bn.easyscanner.content.ContactInfoParcelable
import com.bn.easyscanner.content.EmailParcelable
import com.bn.easyscanner.content.GeoPointParcelable
import com.bn.easyscanner.content.PersonNameParcelable
import com.bn.easyscanner.content.PhoneParcelable
import com.bn.easyscanner.content.QRContent.CalendarEvent
import com.bn.easyscanner.content.QRContent.ContactInfo
import com.bn.easyscanner.content.QRContent.Email
import com.bn.easyscanner.content.QRContent.GeoPoint
import com.bn.easyscanner.content.QRContent.Sms
import com.bn.easyscanner.content.QRContent.Url
import com.bn.easyscanner.content.QRContent.Wifi
import com.bn.easyscanner.content.SmsParcelable
import com.bn.easyscanner.content.UrlBookmarkParcelable
import com.bn.easyscanner.content.WifiParcelable


internal fun Intent?.toQuickieContentType(): QRContent {
  val rawBytes = this?.getByteArrayExtra(EXTRA_RESULT_BYTES)
  val rawValue = this?.getStringExtra(EXTRA_RESULT_VALUE)
  return this?.toQuickieContentType(rawBytes, rawValue) ?: QRContent.Plain(rawBytes, rawValue)
}

@Suppress("LongMethod")
private fun Intent.toQuickieContentType(rawBytes: ByteArray?, rawValue: String?): QRContent? {
  return when (extras?.getInt(QRScannerActivity.EXTRA_RESULT_TYPE, Barcode.TYPE_UNKNOWN)) {
    Barcode.TYPE_CONTACT_INFO -> {
      IntentCompat.getParcelableExtra(this, EXTRA_RESULT_PARCELABLE, ContactInfoParcelable::class.java)?.let {
        ContactInfo(
          rawBytes = rawBytes,
          rawValue = rawValue,
          addresses = it.addressParcelables.map { address -> address.toAddress() },
          emails = it.emailParcelables.map { mail -> mail.toEmail(rawBytes, rawValue) },
          name = it.nameParcelable.toPersonName(),
          organization = it.organization,
          phones = it.phoneParcelables.map { phone -> phone.toPhone(rawBytes, rawValue) },
          title = it.title,
          urls = it.urls
        )
      }
    }
    Barcode.TYPE_EMAIL -> {
      IntentCompat.getParcelableExtra(this, EXTRA_RESULT_PARCELABLE, EmailParcelable::class.java)?.let {
        Email(
          rawBytes = rawBytes,
          rawValue = rawValue,
          address = it.address,
          body = it.body,
          subject = it.subject,
          type = Email.EmailType.entries.getOrElse(it.type) { Email.EmailType.UNKNOWN }
        )
      }
    }
    Barcode.TYPE_PHONE -> {
      IntentCompat.getParcelableExtra(this, EXTRA_RESULT_PARCELABLE, PhoneParcelable::class.java)?.let {
        QRContent.Phone(
          rawBytes = rawBytes,
          rawValue = rawValue,
          number = it.number,
          type = QRContent.Phone.PhoneType.entries.getOrElse(it.type) { QRContent.Phone.PhoneType.UNKNOWN }
        )
      }
    }
    Barcode.TYPE_SMS -> {
      IntentCompat.getParcelableExtra(this, EXTRA_RESULT_PARCELABLE, SmsParcelable::class.java)?.let {
        Sms(
          rawBytes = rawBytes,
          rawValue = rawValue,
          message = it.message,
          phoneNumber = it.phoneNumber
        )
      }
    }
    Barcode.TYPE_URL -> {
      IntentCompat.getParcelableExtra(this, EXTRA_RESULT_PARCELABLE, UrlBookmarkParcelable::class.java)?.let {
        Url(
          rawBytes = rawBytes,
          rawValue = rawValue,
          title = it.title,
          url = it.url
        )
      }
    }
    Barcode.TYPE_WIFI -> {
      IntentCompat.getParcelableExtra(this, EXTRA_RESULT_PARCELABLE, WifiParcelable::class.java)?.let {
        Wifi(
          rawBytes = rawBytes,
          rawValue = rawValue,
          encryptionType = it.encryptionType,
          password = it.password,
          ssid = it.ssid
        )
      }
    }
    Barcode.TYPE_GEO -> {
      IntentCompat.getParcelableExtra(this, EXTRA_RESULT_PARCELABLE, GeoPointParcelable::class.java)?.let {
        GeoPoint(
          rawBytes = rawBytes,
          rawValue = rawValue,
          lat = it.lat,
          lng = it.lng
        )
      }
    }
    Barcode.TYPE_CALENDAR_EVENT -> {
      IntentCompat.getParcelableExtra(this, EXTRA_RESULT_PARCELABLE, CalendarEventParcelable::class.java)?.let {
        CalendarEvent(
          rawBytes = rawBytes,
          rawValue = rawValue,
          description = it.description,
          end = it.end.toCalendarEvent(),
          location = it.location,
          organizer = it.organizer,
          start = it.start.toCalendarEvent(),
          status = it.status,
          summary = it.summary
        )
      }
    }
    else -> null
  }
}

internal fun Intent?.getRootException(): Exception {
  return this?.let { IntentCompat.getParcelableExtra(it, EXTRA_RESULT_EXCEPTION, Exception::class.java) }
    ?: IllegalStateException("Could retrieve root exception")
}

private fun PhoneParcelable.toPhone(rawBytes: ByteArray?, rawValue: String?) =
  QRContent.Phone(
    rawBytes = rawBytes,
    rawValue = rawValue,
    number = number,
    type = QRContent.Phone.PhoneType.entries.getOrElse(type) { QRContent.Phone.PhoneType.UNKNOWN }
  )

private fun EmailParcelable.toEmail(rawBytes: ByteArray?, rawValue: String?) =
  Email(
    rawBytes = rawBytes,
    rawValue = rawValue,
    address = address,
    body = body,
    subject = subject,
    type = Email.EmailType.entries.getOrElse(type) { Email.EmailType.UNKNOWN }
  )

private fun AddressParcelable.toAddress() =
  ContactInfo.Address(
    addressLines = addressLines,
    type = ContactInfo.Address.AddressType.entries.getOrElse(type) { ContactInfo.Address.AddressType.UNKNOWN }
  )

private fun PersonNameParcelable.toPersonName() =
  ContactInfo.PersonName(
    first = first,
    formattedName = formattedName,
    last = last,
    middle = middle,
    prefix = prefix,
    pronunciation = pronunciation,
    suffix = suffix
  )

private fun CalendarDateTimeParcelable.toCalendarEvent() =
  CalendarEvent.CalendarDateTime(
    day = day,
    hours = hours,
    minutes = minutes,
    month = month,
    seconds = seconds,
    year = year,
    utc = utc
  )