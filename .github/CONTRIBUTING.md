# Contributing
We encourage community participation and contributions.
By participating in this project, you agree to abide by the Code of Conduct

All pull requests are welcome, and will be considered at the discretion of the maintainers.

## Contributing Code
In order to maintain the quality and cohesiveness of our codebase,
we ask you to keep the following in mind when contributing

### Code Style
All source code and pull requests should more or less follow the [Google Java Style Guide].
Notable deviations include:

- 4 space indents
- 120 character line limit

Maybe one day there'll be an autoformatter that's not a pain to use, but for now we'll just not be too strict.

### Git
We follow [Gitflow] as our branching model.
All pull requests should branch from `dev` to `feature/<name>`, and the pull request should be made back into `dev`.

Commit messages should be descriptive and meaningful, with the optional body to explain any reasoning.

Commits should not modify anything outside the scope of what they are meant to achieve.

Commit messages should be imperative. For example `Add xyz` and *not* `Added xyz`

The only deviation we make from Gitflow is allowing small, atomic commits directly to `dev`.

[Google Java Style Guide]: https://google.github.io/styleguide/javaguide.html
[gitflow]: https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow
