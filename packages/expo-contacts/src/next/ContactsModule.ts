import { NativeModule, Platform, requireNativeModule } from 'expo-modules-core';
import { Contact as ContactType } from './types/Contact.type';
import {
  ContactDetails,
  ContactField,
  ContactFieldKey,
  CreateContactRecord,
} from './types/ContactProps.type';

declare class ExpoContactsModule extends NativeModule {
  Contact: typeof ContactType;
}

const expoContactsModule = requireNativeModule<ExpoContactsModule>('ExpoContactsNext');

if (Platform.OS === 'ios') {
  expoContactsModule.Contact = expoContactsModule.ContactNext;
}

export class Contact extends expoContactsModule.Contact {
  static async requestPermissionsAsync(): Promise<{ granted: boolean }> {
    return await expoContactsModule.requestPermissionsAsync();
  }

  static async create(contact: CreateContactRecord): Promise<ContactType> {
    return await expoContactsModule.createContact(contact);
  }
  static async getAll(): Promise<Contact[]> {
    return await expoContactsModule.getAllContacts();
  }

  static async addWithForm(contact: CreateContactRecord): Promise<Boolean> {
    return await expoContactsModule.addWithFormContact(contact);
  }

  static async pick(): Promise<Contact> {
    return await expoContactsModule.pickContact();
  }
  // static async getAllWithDetails<T extends readonly ContactField[]>(
  //   fields: T
  // ): Promise<
  //   {
  //     [K in T[number] as ContactFieldKey[K]]: ContactDetails[ContactFieldKey[K]];
  //   }[]
  // > {
  //   return await expoContactsModule.getAllWithDetailsContacts(fields);
  // }
}
