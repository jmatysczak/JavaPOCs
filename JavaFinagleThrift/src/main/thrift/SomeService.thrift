namespace java jmat.javapocs.javafinaglethrift.model
namespace scala jmat.javapocs.javafinaglethrift.model

struct SomeRequest {
    1: string firstName,
    2: string lastName
}

struct SomeResponse {
    1: string message
}

service SomeService {
    SomeResponse someOperation(1:SomeRequest request)
}