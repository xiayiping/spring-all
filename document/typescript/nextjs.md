<!-- TOC -->
  * [Components](#components)
    * [Server Components](#server-components)
    * [Client Components](#client-components)
  * [Routing Convention](#routing-convention)
    * [path parameter](#path-parameter)
    * [path parameter list](#path-parameter-list)
    * [Path Group](#path-group)
  * [Layouts](#layouts)
<!-- TOC -->

## Components

### Server Components

- rendered on server
- never sent to client
- can't use interactive feature like useState or useEffect
- default component

### Client Components

- rendered on browser
- or rendered to HTML once on the server which:
- allows the user to see the page immediately rather than a blank page.
- can use state, effects, and browser APIs.
- add 'use client' directive on the top of file


## Routing Convention

- All must in 'app' folder
- index page is named as page.tsx

### path parameter

- folder name: 

```
/product/[productId]/view/[viewId]  
``` 

```typescript
// page.tsx
export default function ProductReviewDetail({params}: {
    params: {
        productId: string,
        reviewId: string,
    }
}) {
    if (parseInt(params.productId) <= 0 || parseInt(params.reviewId) <= 0) {
        notFound();
    }
    return <>
        <h1>product - review detail {params.productId} - {params.reviewId}</h1>
    </>
}
```

```typescript
// or route.tsx

export async function GET(  // or POST ...
    req: Request,
    {params}: {
        params: {
            productId: string,
            reviewId: string,
        }
    }
) {
    console.log(req)
    const newUser = {
        id: 199, name: "Marry",
    }
    users.push(newUser)
    return Response.json(users)
}


export async function GET(request) {
    try {
        // Parse the URL of the incoming request
        const {searchParams} = new URL(request.url);

        // Extract query parameters
        const name = searchParams.get('name');
        const age = searchParams.get('age');
        // ....
    } catch (error) {
    
    }
}
```

### path parameter list

- folder name:
```
/path/[...slugs]
```
```typescript
export default function DocsSlug({params}: {
    params: {
        slugs: string[]
    }
}) {
    return <>
        <h1>Docs Home {params.slugs[0]} {params.slugs[1]} {params.slugs[2]} {params.slugs[3]}</h1>
    </>
}
```

### Path Group

- folder named with `()` will be considered as 'group path', it doesn't show in url, just for grouping paths in same logic concept. like `(auth)` 


## Layouts

- layout.tsx
  - `children` means the `page.tsx` in route folder

```typescript


export default function RootLayout({
                                       children,
                                   }: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="en">
        <body><header>
            <p>Header</p>
        </header>
        {children}
        <footer>
            <p>Footer</p>
        </footer>
        </body>
        </html>
    );
}
// mandatory layout for all next.js application
```

- also cal create nested layout for a specific route folder