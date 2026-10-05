# Spring Boot REST Controllers Reference

## 1. Controller Fundamentals
- `@RestController` combines `@Controller` and `@ResponseBody`.
- Maps HTTP requests to handler methods.

## 2. Request Mapping Annotations
- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`.
- Use `@PathVariable` for URI path variables.
- Use `@RequestParam` for query parameters.
- Use `@RequestBody` to deserialize JSON payload into Java objects.

## 3. Response Handling
- Return `ResponseEntity<T>` to set status codes, headers, and body cleanly.
