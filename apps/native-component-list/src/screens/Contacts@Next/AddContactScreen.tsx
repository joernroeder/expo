import React, { useEffect, useState } from 'react';
import { View, Text, Button, ScrollView, Platform } from 'react-native';
import { Contact, ContactDetails, ContactField, PartialContactDetails } from 'expo-contacts/next';
import { getContactsAsync } from 'expo-contacts';

const AddContactScreen = () => {
  const [contacts, setContacts] = useState<Contact[]>([]);
  const fields = [ContactField.DISPLAY_NAME, ContactField.PHONES, ContactField.EMAILS] as const;
  const [details, setDetails] = useState<PartialContactDetails<typeof fields>[]>([]);

  const addContact = async () => {
    const contactDetails: ContactDetails = {
      displayName: 'Jan Kowalski',
      givenName: 'Jan',
      middleName: 'Marek',
      familyName: 'Kowalski',
      prefix: 'Pan',
      suffix: 'Jr.',
      phoneticGivenName: 'Yan',
      phoneticMiddleName: 'Marek',
      phoneticFamilyName: 'Kovalskee',
      company: 'Przykładowa Firma',
      department: 'Dział IT',
      jobTitle: 'Programista',
      ...(Platform.OS === 'android' ? { isFavourite: true } : {}),
      note: 'Ważny klient. Kontaktować się w godzinach 9-17.',
      emails: [
        { label: 'praca', email: 'jan.kowalski@example.com' },
        { label: 'prywatny', email: 'jan.k@gmail.com' },
      ],
      phones: [
        { label: 'komórka', number: '+48 123 456 789', countryCode: 'pl', digits: '123456789' },
        { label: 'praca', number: '+48 12 987 65 43', countryCode: 'pl', digits: '129876543' },
      ],

      // addresses: [
      //   {
      //     label: 'dom',
      //     street: 'ul. Floriańska 15/3',
      //     city: 'Kraków',
      //     region: 'małopolskie',
      //     postcode: '31-019',
      //     country: 'Polska',
      //   },
      // ],

      // dates: [
      //   { label: 'urodziny', day: 10, month: 5, year: 1990 },
      //   { label: 'rocznica', day: 22, month: 8, year: 2015 },
      // ],

      // imAddresses: [
      //   { label: 'slack', service: 'Slack', username: 'jan.kowalski' },
      //   { label: 'skype', service: 'Skype', username: 'jankowalski90' },
      // ],

      // relationships: [
      //   { label: 'małżonka', name: 'Anna Kowalska' },
      //   { label: 'asystent', name: 'Piotr Nowak' },
      // ],

      // urlAddresses: [
      //   { label: 'portfolio', url: 'https://jankowalski.dev' },
      //   { label: 'linkedin', url: 'https://linkedin.com/in/jankowalski' },
      // ],

      // extraNames: [{ label: 'pseudonim', name: 'Janek' }],
    };
    await Contact.create(contactDetails);
    await fetchContacts();
  };

  const getDetails = async () => {
    const result = await Contact.getAllWithDetails(fields);
    const oldResult = await getContactsAsync();
    console.log('Old API result:', oldResult.data[0].phoneNumbers);
    setDetails(result);
  };

  const fetchContacts = async () => {
    const contacts = await Contact.getAll();
    setContacts(contacts);
  };

  useEffect(() => {
    fetchContacts();
  }, []);

  return (
    <View style={{ flex: 1, padding: 20 }}>
      <Text>Add Contact Screen</Text>
      <Button title="Add Contact" onPress={addContact} />
      <Button title="FetchDetails" onPress={getDetails} />
      <Text>Contacts List:</Text>
      {contacts.map((contact, index) => (
        <Text key={index}>{contact.id}</Text>
      ))}
      <ScrollView style={{ flex: 1 }}>
        <Text>{JSON.stringify(details, null, 2)}</Text>
      </ScrollView>
    </View>
  );
};

export default AddContactScreen;
