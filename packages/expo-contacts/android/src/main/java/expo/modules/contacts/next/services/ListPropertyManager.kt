package expo.modules.contacts.next.services

import expo.modules.contacts.next.domain.model.Extractable
import expo.modules.contacts.next.domain.ContactRepository
import expo.modules.contacts.next.domain.model.ExtractableField
import expo.modules.contacts.next.domain.wrappers.ContactId
import expo.modules.contacts.next.domain.wrappers.DataId
import expo.modules.contacts.next.mappers.ContactRecordDomainMapper
import expo.modules.contacts.next.records.ExistingRecord
import expo.modules.contacts.next.records.NewRecord

class ListPropertyManager<
  TExistingModel: Extractable,
  TNewRecord: NewRecord,
  TExistingRecord: ExistingRecord,
  >(
  private val extractableField: ExtractableField<TExistingModel>,
  private val contactId: ContactId,
  private val repository: ContactRepository,
  private val mapper: ContactRecordDomainMapper
) {
  suspend fun getAll(): List<TExistingRecord> =
    repository.getById(extractableField, contactId)
      .map { mapper.toRecord(it) }

  suspend fun add(record: TNewRecord): String {
    val rawContactId = repository.getRawContactId(contactId)
      ?: throw Exception("Unable to find default rawContactId, create a local contact")
    return repository.appendField(mapper.toAppendable(record, rawContactId)).value
  }

  suspend fun update(record: TExistingRecord) =
    repository.updateField(mapper.toUpdatable(record))

  suspend fun delete(record: TExistingRecord) =
    repository.deleteField(DataId(record.id))
}