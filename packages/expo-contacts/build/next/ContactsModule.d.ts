import { NativeModule } from 'expo-modules-core';
import { Contact as ContactType } from './types/Contact.type';
import { ContactProps } from './types/ContactProps.type';
declare class ExpoContactsModule extends NativeModule {
    Contact: typeof ContactType;
}
declare const expoContactsModule: ExpoContactsModule;
export declare class Contact extends expoContactsModule.Contact {
    static create(contact: ContactProps): Promise<ContactType>;
}
export {};
//# sourceMappingURL=ContactsModule.d.ts.map