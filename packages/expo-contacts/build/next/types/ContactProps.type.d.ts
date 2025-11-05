export type Email = {
    label?: string;
    email?: string;
};
export type Phone = {
    label?: string;
    number?: string;
};
export type Address = {
    label?: string;
    street?: string;
    city?: string;
    region?: string;
    postcode?: string;
    country?: string;
};
export type ContactProps = {
    firstName?: string;
    middleName?: string;
    lastName?: string;
    displayName?: string;
    nickname?: string;
    company?: string;
    jobTitle?: string;
    emails?: Email[];
    phones?: Phone[];
    addresses?: Address[];
    websites?: string[];
    birthday?: string;
    note?: string;
    photoUri?: string;
    groups?: string[];
};
//# sourceMappingURL=ContactProps.type.d.ts.map