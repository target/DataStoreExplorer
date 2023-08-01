package com.target.datastorexplorer.fileType.proto

import com.intellij.openapi.fileTypes.LanguageFileType
import com.target.datastorexplorer.PROTO_EXT
import javax.swing.Icon

private const val PROTO_NAME = "Generated proto file"
private const val PROTO_DESC = "Generated proto file"

class ProtoFileType : LanguageFileType(ProtoLanguage) {
	override fun getName(): String = PROTO_NAME

	override fun getDescription() = PROTO_DESC

	override fun getDefaultExtension() = PROTO_EXT

	override fun getIcon(): Icon? = null
}
