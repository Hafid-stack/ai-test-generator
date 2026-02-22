package org.example.aitestgenerator.dto;

public record TestRequest(
        String methodName,
        String fullClassCode
)
{
}
