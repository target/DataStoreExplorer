package com.target.datastorexplorer.decoder

import com.github.os72.protocjar.Protoc
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.Descriptors
import com.google.protobuf.DynamicMessage
import com.google.protobuf.util.JsonFormat
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.target.datastorexplorer.coroutine.CoroutineDispatchers
import com.target.datastorexplorer.localization.DataStoreBundle
import com.target.datastorexplorer.notification.NotificationHelper
import com.target.datastorexplorer.notification.notifyError
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.file.FileSystems

/**
 * @param project current project
 * @param protoBuffFile proto content file (.pb)
 * @param dispatchers coroutine dispatchers
 */
class ProtoDecoder(
	private val project: Project,
	private val protoBuffFile: VirtualFile,
	private val dispatchers: CoroutineDispatchers,
) {

	private val logger by lazy { Logger.getInstance(ProtoDecoder::class.java) }

	/**
	 * @param protoDefinitionFile .proto file
	 * @return the deserialized string format.
	 */
	suspend fun decodeProto(protoDefinitionFile: VirtualFile): String {
		return withContext(dispatchers.default()) {
			try {
				val parsedProtoBytes = binaryProtobufContent(protoDefinitionFile)
				val fileDescriptor: Descriptors.FileDescriptor = parseFileDescriptorSet(
					parsedProtoBytes,
					selectedProtoFile = protoDefinitionFile.path.split(FileSystems.getDefault().separator)
						.last { it.isNotBlank() }
				)
				val dynamicMessage =
					DynamicMessage.parseFrom(fileDescriptor.messageTypes[0], this@ProtoDecoder.protoBuffFile.inputStream.readBytes())
				val printer = JsonFormat.printer().print(dynamicMessage)
				printer
			} catch (ex: ArrayIndexOutOfBoundsException) {
				NotificationHelper.notifyInfo(
					project = project,
					content = DataStoreBundle.message("notification.proto.wrong")
				)
				""
			} catch (ex: Exception) {
				logger.debug(ex)
				ex.notifyError(project, DataStoreBundle.message("notification.deserialization.failed"))
				""
			}
		}
	}

	// Proto DataStore to work requires a pre-defined schema - app/src/main/proto/
	// ref - https://developer.android.com/topic/libraries/architecture/datastore#proto-schema
	private fun binaryProtobufContent(protoFile: VirtualFile): ByteArray {
		val directoryPath = if (protoFile.path.contains("/proto")) {
			protoFile.path.substringBefore("/proto").plus("/proto")
		} else {
			protoFile.parent.path
		}
		val args = arrayOf(
			"-v:com.google.protobuf:protoc:3.17.3",
			"-I=$directoryPath",
			"--include_imports",
			"--descriptor_set_out=/dev/stdout",
			protoFile.path
		)
		val outputStream = ByteArrayOutputStream()
		val errorStream = ByteArrayOutputStream()
		Protoc.runProtoc(args, outputStream, errorStream)
		return outputStream.toByteArray()
	}

	private fun parseFileDescriptorSet(
		protoBytes: ByteArray,
		selectedProtoFile: String,
	): Descriptors.FileDescriptor {
		val fileDescriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(protoBytes)
		val fileDescriptors = fileDescriptorSet.fileList.map { fileDescriptorProto ->
			val dependencyDescriptors = fileDescriptorProto.dependencyList.map { dependencyFileName ->
				Descriptors.FileDescriptor.buildFrom(
					fileDescriptorSet.fileList.find { it.name == dependencyFileName },
					emptyArray()
				)
			}.toTypedArray()
			Descriptors.FileDescriptor.buildFrom(fileDescriptorProto, dependencyDescriptors)
		}.toTypedArray()

		return Descriptors.FileDescriptor.buildFrom(
			fileDescriptorSet.fileList.find { it.name.split("/").last().contentEquals(selectedProtoFile)},
			fileDescriptors
		)
	}
}
