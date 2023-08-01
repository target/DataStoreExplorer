package com.target.datastorexplorer.fileType.preferences

import com.intellij.openapi.fileTypes.LanguageFileType
import com.target.datastorexplorer.PREFERENCE_EXT
import javax.swing.Icon

private const val PREFERENCE_NAME = "Generated preference file"
private const val PREFERENCE_DESC = "Generated preference file"

class PreferencesFileType : LanguageFileType(PreferencesLanguage) {

	override fun getName(): String = PREFERENCE_NAME

	override fun getDescription(): String = PREFERENCE_DESC

	override fun getDefaultExtension(): String = PREFERENCE_EXT

	override fun getIcon(): Icon? = null
}