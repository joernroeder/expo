import ExpoModulesCore
import Contacts
import ContactsUI

public class ContactsNextModule: Module {
  private let contactStore = CNContactStore()
  
  public func definition() -> ModuleDefinition {
    Name("ExpoContactsNext")
    Class(ContactNext.self) {
      Constructor { (id: String) -> ContactNext in
        return ContactNext(id: id)
      }
      Property("id") { (this: ContactNext) in
        return this.id
      }
    }
    
    AsyncFunction("createContact") { (contactDetails: ContactDetails) -> ContactNext in
      let saveRequest = CNSaveRequest()
      let newContact = contactDetails.toCNMutableContact()
      saveRequest.add(newContact, toContainerWithIdentifier: nil)
      try contactStore.execute(saveRequest)
      return ContactNext(id: newContact.identifier)
    }
    
    AsyncFunction("getAllContacts") {
      let request = CNContactFetchRequest(keysToFetch: [CNContactIdentifierKey as CNKeyDescriptor])
      var fetchedContacts: [CNContact] = []
      try self.contactStore.enumerateContacts(with: request) { contact, _ in
        fetchedContacts.append(contact)
      }
      return fetchedContacts.map { ContactNext(id: $0.identifier)}
    }
    
    AsyncFunction("getAllWithDetailsContacts") { (fields: [ContactField]) -> [ContactDetails] in
      let keys = fields.flatMap { $0.contactKey }
      let keyDescriptors: [CNKeyDescriptor] = keys.map { $0 as NSString }

      let request = CNContactFetchRequest(keysToFetch: keyDescriptors)
      var fetchedContacts: [CNContact] = []

      try self.contactStore.enumerateContacts(with: request) { contact, _ in
          fetchedContacts.append(contact)
      }

      return fetchedContacts.map { $0.toContactDetails(fetchedKeys: Set(keys)) }
    }
  }
}
