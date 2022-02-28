import { fileLoader, mergeTypes, mergeResolvers } from 'merge-graphql-schemas'
import { makeExecutableSchema } from 'graphql-tools'
import { globOptions, findFiles, importFile } from '@druidjs/path-utils'

function loadScalars (scalarsPath: string) {
  const typeDefs = []
  const resolvers = {}
  findFiles(scalarsPath).forEach((scalarPath : any) => {
    const scalars = importFile(scalarPath)
    Object.keys(scalars).forEach(scalarName => {
      typeDefs.push(`scalar ${scalarName}`)
      resolvers[scalarName] = scalars[scalarName]
    })
  })
  return { typeDefs, resolvers }
}

export default function loadSchema({ modulePaths }) {
  const schema = {} as any 

  const scalars = loadScalars(modulePaths.scalars)

  schema.typeDefs = mergeTypes([
    ...scalars.typeDefs,
    ...fileLoader(modulePaths.typeDefs, globOptions)
  ])

  schema.resolvers = mergeResolvers([
    scalars.resolvers,
    ...fileLoader(modulePaths.resolvers, globOptions)
  ])

  let executableSchema 
  try {
    executableSchema = makeExecutableSchema(schema)
  } catch(err) {
    console.log('Error while loading models' + err)
    throw (err)
  }

  return executableSchema
}
