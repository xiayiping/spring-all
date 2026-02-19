type RGB = `#${string}` | [number, number, number]

const colors = {
    red: "#F00",
    blue: [0, 0, 255]
} satisfies Record<string, RGB>

console.log(colors.red.toUpperCase())

/////////////////////

type Grade = 'a' | 'b' | 'c'

function check(g: Grade) {
    switch (g) {
        case "a":
            return "A"
        case "b":
            return "B"
        case "c":
            return "C"
        default:
            // will check g is fully covered in switch
            return `${g satisfies never}`
    }
}

////////////////////////

type User = {
    id: string,
    name: string,
    age: number,
    address: {
        street: string,
        city: string,
    },
    aliasName? : string
}

function createUser(user: Omit<User, "id">) {
    // accept a user without id
}

function updateUser(user: Partial<User>) {
    // change all fields in user to optional
}

function renderUser(user: Pick<User, "name" | "age">) {
    // only need to provide name and age
}

function requireFields(user: Required<User>) {
    // now all fields are required no matter whether originally it's optional
}

function readOnlyUser(user: Readonly<User>) {
    // now all fields are read only.
}