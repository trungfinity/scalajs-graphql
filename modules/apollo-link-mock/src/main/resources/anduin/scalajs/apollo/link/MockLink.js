const { ApolloLink, Observable } = require('apollo-link');
const { graphql, print } = require('graphql');

class MockLink extends ApolloLink {
  constructor({ schema, rootValue, context }) {
    super();
    this.schema = schema;
    this.rootValue = rootValue;
    this.context = context;
  }

  request(operation) {
    return new Observable(observer => {
      graphql(
        this.schema,
        print(operation.query),
        this.rootValue,
        this.context,
        operation.variables,
        operation.operationName
      ).then(
        data => {
          if (!observer.closed) {
            observer.next(data);
            observer.complete();
          }
        },
        error => {
          if (!observer.closed) {
            observer.error(error);
          }
        }
      );
    });
  }
}

exports.MockLink = MockLink;
